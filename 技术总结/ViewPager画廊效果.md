# ViewPager的画廊效果
ViewPager和ViewPager2存在setPageTransformer方法，用于对item进行变换

```kotlin
// 中间item是缩放倍率
        val maxScale = 1f
        // 两侧item缩放倍率
        val miniScale = resources.getFraction(R.fraction.theme_big_image_scale_factory.getResourceByScale(), 1, 1)
        // 两侧伸出的长度
        val leftRightOffset = R.dimen.theme_large_image_left_right_offset.getResourceByScale().dimenResource()
        // 用于留出左右两侧足够的区域显示两侧item
        binding.vp.getChildAt(0).setPadding(leftRightOffset,0,leftRightOffset,0)
        (binding.vp.getChildAt(0) as ViewGroup).clipToPadding = false
        binding.vp.setPageTransformer { page, position ->
            // 不要对其它page做变换，否则会导致offscreenPageLimit缓存失效
            if (abs(position) >= 2f) {
                page.translationX = 0f
                return@setPageTransformer
            }
            val offset = page.width * (maxScale - miniScale) / 2 + leftRightOffset
            page.translationX = -position * page.measuredWidth
            if (position != 0f) {
                page.translationX += min(abs(position), 1f) * offset * position / abs(position)
            }
            if (abs(position) > miniScale) {
                page.scaleX = miniScale
            } else if (position != 0f) {
                page.scaleX = -(maxScale - miniScale) / miniScale * abs(position) + maxScale
            } else {
                page.scaleX = maxScale
            }

            page.translationZ = 100 - abs(position)
            page.scaleY = page.scaleX
        }
```
![画廊效果图.jpg](res%2F%E7%94%BB%E5%BB%8A%E6%95%88%E6%9E%9C%E5%9B%BE.jpg)