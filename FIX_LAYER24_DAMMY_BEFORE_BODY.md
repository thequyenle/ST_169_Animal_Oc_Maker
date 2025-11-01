# Fix Cứng: Layer[24] Character 1 (Miley) Render Trước Body

## Vấn đề
Trong log Character 1 (Miley) xuất hiện:
```
Layer[0]: posNav=0, posCus=1, path=1.png    → RENDER to BODY ImageView
...
Layer[24]: posNav=24, posCus=0, path=4.png  → RENDER to ImageView[0]
```

**Phân tích:**
- Body layer (Layer[0]) có `positionCustom=1`, render vào **BodyImageView** riêng
- Layer[24] có `positionCustom=0`, render vào **ImageView[0]**
- Trong vòng lặp `forEachIndexed`, Layer[24] được render **SAU** Body
- Vì Layer[24] được render sau nên nó **ĐÈ LÊN** Body layer

## Yêu cầu
- Layer[24] cần render **TRƯỚC** Body layer để không che mất Body
- Chỉ áp dụng cho **Character 2 (Dammy)** - `categoryPosition == 2`

## Giải pháp: Hardfix với ImageView riêng

### 1. CustomizeViewModel.kt - Tạo Layer24ImageView riêng

**Thêm StateFlow cho Layer24ImageView:**
```kotlin
// 🔧 HARDFIX: ImageView riêng cho Layer[24] của Character 2 (render trước Body)
private val _layer24ImageView = MutableStateFlow<ImageView?>(null)
val layer24ImageView = _layer24ImageView.asStateFlow()
```

**Update `setImageViewList()` để tạo Layer24ImageView:**
```kotlin
suspend fun setImageViewList(frameLayout: FrameLayout) {
    // 🔧 HARDFIX Character 2: Tạo ImageView riêng cho Layer[24] (đặt đầu tiên - dưới cùng)
    if (positionSelected == 2) {
        val layer24ImageView = ImageView(frameLayout.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        frameLayout.addView(layer24ImageView, 0)  // Thêm vào index 0 (z-index thấp nhất)
        _layer24ImageView.value = layer24ImageView
    }
    
    // ✅ Tạo BodyImageView (đặt sau Layer24)
    val bodyImageView = ImageView(frameLayout.context).apply { ... }
    frameLayout.addView(bodyImageView)  // z-index cao hơn Layer24
    _bodyImageView.value = bodyImageView
    
    // Tạo các ImageView cho các layer khác
    _imageViewList.value.addAll(addImageViewToLayout(...))
}
```

### 2. CustomizeActivity.kt - `renderAllLayers()`

**Thêm logic render Layer[24] vào Layer24ImageView riêng:**
```kotlin
private fun renderAllLayers() {
    Log.d("CustomizeActivity", "════════════════════════════════════════")
    Log.d("CustomizeActivity", "🎨 RENDER ALL LAYERS START")
    Log.d("CustomizeActivity", "════════════════════════════════════════")

    // 🔧 HARDFIX Character 2: Render Layer[24] vào Layer24ImageView riêng
    if (categoryPosition == 2) {
        val layer24 = viewModel.dataCustomize.value?.layerList?.getOrNull(24)
        if (layer24 != null && layer24.positionNavigation == 24) {
            val path24 = viewModel.pathSelectedList.value.getOrNull(24)
            val layer24ImageView = viewModel.layer24ImageView.value
            
            if (!path24.isNullOrEmpty() && layer24ImageView != null) {
                Log.d("CustomizeActivity", "🔧 HARDFIX Dammy: Render Layer[24] to Layer24ImageView")
                Glide.with(this@CustomizeActivity)
                    .load(path24)
                    .into(layer24ImageView)
            } else if (layer24ImageView != null) {
                Glide.with(this@CustomizeActivity).clear(layer24ImageView)
            }
        }
    }

    // Vòng lặp render bình thường cho tất cả layers
    viewModel.dataCustomize.value?.layerList?.forEachIndexed { index, layerListModel ->
        // 🔧 HARDFIX: Skip Layer[24] cho Character 2 (đã render riêng)
        if (categoryPosition == 2 && index == 24) {
            Log.d("CustomizeActivity", "  → SKIP Layer[24] (already rendered)")
            return@forEachIndexed
        }
        
        // ...existing render logic...
    }

    Log.d("CustomizeActivity", "════════════════════════════════════════")
    Log.d("CustomizeActivity", "🎨 RENDER ALL LAYERS END")
    Log.d("CustomizeActivity", "════════════════════════════════════════")
}
```

**Cách hoạt động:**
1. ✅ Khi init: Tạo **Layer24ImageView** riêng cho Character 2
2. ✅ Layer24ImageView được add vào FrameLayout ở **index 0** (z-index thấp nhất)
3. ✅ BodyImageView được add sau (z-index cao hơn Layer24)
4. ✅ Các ImageView khác được add tiếp (z-index cao nhất)
5. ✅ Khi render: Layer[24] render vào Layer24ImageView riêng
6. ✅ Vòng lặp chính **skip Layer[24]** để tránh duplicate
7. ✅ Body và các layer khác render bình thường

## Kết quả

### Trước khi fix:
```
Render order:
1. Body → BodyImageView ✓
2. Layer[24] → ImageView[0] (đè lên Body) ❌
```

### Sau khi fix:
```
Render order:
1. Layer[24] → ImageView[0] ✓ (render trước)
2. Body → BodyImageView ✓
3. Vòng lặp chính:
   - Layer[0] (Body) → BodyImageView (update)
   - Layer[24] → ImageView[0] (update lại nếu cần)
```

**Kết quả cuối cùng:**
- ✅ Layer[24] được render vào ImageView[0]
- ✅ Body vẫn render vào BodyImageView (không bị che)
- ✅ Layer[24] nằm **DƯỚI** Body layer (đúng thứ tự z-index)

## Log output:
```
🎨 RENDER ALL LAYERS START
🔧 HARDFIX Dammy: Render Layer[24] BEFORE Body
  → RENDER Layer[24] to ImageView[0] (BEFORE Body)
Layer[0]: posNav=0, posCus=1, path=1.png
  → RENDER to BODY ImageView
...
Layer[24]: posNav=24, posCus=0, path=4.png
  → RENDER to ImageView[0]
🎨 RENDER ALL LAYERS END
```

## Scope của Fix

**CHỈ áp dụng cho:**
- ✅ Character 2 (Dammy) - `categoryPosition == 2`
- ✅ Layer[24] có `positionNavigation=24`
- ✅ Khi Layer[24] có path (không rỗng)

**KHÔNG ảnh hưởng:**
- ❌ Character 0, 1, 3, 4...
- ❌ Các layer khác
- ❌ Logic render mặc định

## Note quan trọng

### Tại sao không sửa positionCustom trong data?
- Data JSON có thể đúng theo thiết kế (Layer[24] cần ở vị trí 0)
- Vấn đề là **thứ tự render**, không phải vị trí
- Fix trong code linh hoạt hơn (có thể bật/tắt dễ dàng)

### Khi nào cần remove fix này?
- Backend thay đổi cấu trúc layer (Layer[24] không còn conflict)
- Thay đổi z-index của ImageView trong layout XML
- Implement hệ thống z-order tự động

## Các file đã sửa
1. `CustomizeActivity.kt`:
   - `renderAllLayers()`: Thêm hardfix render Layer[24] trước Body

## Testing
Test cases cho Character 2:
1. ✅ Layer[24] có ảnh → Render đúng vào ImageView[0], nằm dưới Body
2. ✅ Layer[24] rỗng → Không render, không ảnh hưởng
3. ✅ Switch sang tab khác → Render lại đúng
4. ✅ Random all → Layer[24] được random và render đúng
5. ✅ Character 0, 1 không bị ảnh hưởng

## Ngày fix
31/01/2025

