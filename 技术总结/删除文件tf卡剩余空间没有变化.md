# 删除文件后tf卡剩余空间没有变化
使用tf时，如果需要时刻检测剩余空间，会使用到File.getFreeSpace方法，
但有时候删除某个文件后，该方法返回值没有变化。这对这个情况，可以对文件先写入空字节再删除，利用write方法会立即更新剩余空间的功能来实现刷剩余空间
```kotlin
fun File.deleteImmediatelyAndFree() {
    try {
        writeBytes(EmptyByteArray)
    } catch (e: Throwable) {
        e.printStackTrace()
    }
    delete()
}
// file.delete()
file.deleteImmediatelyAndFree()
```