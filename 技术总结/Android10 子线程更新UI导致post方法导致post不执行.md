# Android10 子线程更新UI导致post方法导致post不执行

通常情况下，Android是不允许子线程更新UI的，子线程更新UI会直接报异常。
```java
void checkThread() {
        if (mThread != Thread.currentThread()) {
            throw new CalledFromWrongThreadException(
                    "Only the original thread that created a view hierarchy can touch its views.");
        }
    }
```

如果当前执行的线程和mThread不一致，会直接throw一个Exception。也就是说如果没有执行到checkThread方法，则不会抛异常。  
那哪些方法会导致进行线程检查呢？
```
android.view.View#requestFitSystemWindows()
android.view.View#requestLayout()
android.view.View#setVisibility()
android.view.View#requestFocus(int, Rect)
android.view.View#requestLayout
android.view.View#invalidate(boolean) //未开启硬件加速时
.....
还有很多
```
但是有些方法是不会触发线程检测的：
```
当设置的图片前后大小一致时，不会触发requestLayout
android.widget.ImageView#setImageDrawable
// 开启硬件加速
android.view.View#invalidate(boolean)
```

默认情况下，设备都是开启硬件加速的。也就是说在开启应用加速的情况下，可以在子线程调用invalidate、setImageDrawable等方法，而且会生效。  
invalidate最终会调到ViewRootImpl#scheduleTraversals方法。
```java
@UnsupportedAppUsage
    void scheduleTraversals() {
        if (!mTraversalScheduled) {
            // ......
            mTraversalScheduled = true;
            // 向消息队列中加入同步屏障
            mTraversalBarrier = mHandler.getLooper().getQueue().postSyncBarrier();
            // 向消息队列中加入异步回调
            mChoreographer.postCallback(
                    Choreographer.CALLBACK_TRAVERSAL, mTraversalRunnable, null);
            // ......
        }
    }
```
ViewRootImpl#scheduleTraversals方法中存在两个重要的方法调用，设置消息同步屏障和设置异步回调。

何为消息同步屏障？消息同步屏障就是一个target属性为null的Message对象，Message的target就是Handler对象，表示这个消息应该交给谁来执行。
接下来看看消息队列MessageQueue.next方法
```java
Message next() {
    // ......
    Message prevMsg = null;
    Message msg = mMessages;
    // 如果第一个消息是同步屏障则循环，直到找到下一个异步消息。否则就直接是第一个消息
    if (msg != null && msg.target == null) {
        do {
            prevMsg = msg;
            msg = msg.next;
        } while (msg != null && !msg.isAsynchronous());
    }
    // 找打了需要执行的消息
    if (msg != null) {
        // .......
    }
    // ......
}
```
### 为何会有同步屏障消息？
用于消息插队，加速UI显示，同时又能利用等待VSYNC信号的空窗期执行其它任务。

当存在同步消息屏障时，所有的同步消息将被阻塞，直到移除同步消息屏障。
当子线程调用ViewRootImpl#scheduleTraversals方法，会导致android.view.ViewRootImpl#mTraversalBarrier变量线程不安全，存在的隐患就是可能某个同步消息屏障没有移除。从而导致所有的同步消息无法执行。  
也就是说要避免子线程更新UI，某些情况下能更新成功，但会存在同步消息屏障无法取消的情况，从而导致严重问题。


