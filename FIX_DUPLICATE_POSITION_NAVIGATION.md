# Fix: Duplicate positionNavigation gây đè layer

## Vấn đề
Trong log xuất hiện:
```
Layer[20]: posNav=20, posCus=17, path=EMPTY    → CLEAR ImageView[17]
Layer[21]: posNav=20, posCus=22, path=EMPTY    → CLEAR ImageView[22]
```

**Phân tích:**
- **Layer[20]** và **Layer[21]** cùng có `positionNavigation=20`
- Điều này vi phạm logic của hệ thống vì `positionNavigation` phải là unique
- Khi render, Layer[21] sẽ đè lên Layer[20] vì cache chỉ lưu được 1 mapping

## Nguyên nhân

### 1. Cache bị ghi đè
Trong `buildLayerIndexCache()`:
```kotlin
layerList.forEachIndexed { index, layer ->
    cache[layer.positionNavigation] = index  // ❌ Nếu duplicate, cái sau ghi đè!
}
```

Khi có duplicate `positionNavigation=20`:
- Loop đến Layer[20]: `cache[20] = 20`
- Loop đến Layer[21]: `cache[20] = 21` ← **GHI ĐÈ!**
- Kết quả: Layer[20] bị mất khỏi cache

### 2. RenderAllLayers không clear ImageView trước
Trước đây, `renderAllLayers()` không clear ImageView trước khi render mới:
- Nếu Layer[20] đã render vào ImageView[17], sau đó bị skip
- Layer[21] render vào ImageView[22]
- ImageView[17] vẫn giữ ảnh cũ từ Layer[20] → **ĐÈ LÊN** layer mới!

## Giải pháp

### 1. CustomizeViewModel.kt - Detect duplicate positionNavigation

#### Thêm detection và warning
```kotlin
private fun buildLayerIndexCache() {
    val layerList = _dataCustomize.value?.layerList ?: return
    val cache = mutableMapOf<Int, Int>()
    
    layerList.forEachIndexed { index, layer ->
        // ⚠️ Detect duplicate positionNavigation
        if (cache.containsKey(layer.positionNavigation)) {
            Log.e("CustomizeViewModel", "⚠️ DUPLICATE positionNavigation=${layer.positionNavigation}!")
            Log.e("CustomizeViewModel", "   Layer[${cache[layer.positionNavigation]}]: posNav=${layer.positionNavigation}")
            Log.e("CustomizeViewModel", "   Layer[$index]: posNav=${layer.positionNavigation}, posCus=${layer.positionCustom}")
            Log.e("CustomizeViewModel", "   → Using FIRST occurrence (Layer[${cache[layer.positionNavigation]}])")
            // ✅ KHÔNG ghi đè - giữ layer đầu tiên
        } else {
            cache[layer.positionNavigation] = index
        }
    }
    
    _layerIndexCache.value = cache
    
    // 🔍 LOG: Full cache for debugging
    Log.d("CustomizeViewModel", "📋 Layer Index Cache built: ${cache.size} entries")
    cache.entries.sortedBy { it.key }.forEach { (posNav, layerIndex) ->
        val layer = layerList[layerIndex]
        Log.d("CustomizeViewModel", "   posNav=$posNav → Layer[$layerIndex] (posCus=${layer.positionCustom})")
    }
}
```

**Cách fix:**
- ✅ Detect duplicate `positionNavigation` và log ERROR
- ✅ **GIỮ layer đầu tiên** khi có duplicate (không ghi đè)
- ✅ Log toàn bộ cache để debug

### 2. CustomizeActivity.kt - Clear ImageViews trước khi render

#### Thêm clear logic vào renderAllLayers()
```kotlin
private fun renderAllLayers() {
    Log.d("CustomizeActivity", "════════════════════════════════════════")
    Log.d("CustomizeActivity", "🎨 RENDER ALL LAYERS START")
    Log.d("CustomizeActivity", "════════════════════════════════════════")

    // ✅ FIX: Clear tất cả ImageView trước để tránh layers cũ đè lên
    Log.d("CustomizeActivity", "🧹 Clearing all ImageViews...")
    viewModel.bodyImageView.value?.let { Glide.with(this).clear(it) }
    viewModel.imageViewList.value.forEach { imageView ->
        Glide.with(this).clear(imageView)
    }

    viewModel.dataCustomize.value?.layerList?.forEachIndexed { index, layerListModel ->
        // ...existing render logic...
    }
    
    Log.d("CustomizeActivity", "════════════════════════════════════════")
    Log.d("CustomizeActivity", "🎨 RENDER ALL LAYERS END")
    Log.d("CustomizeActivity", "════════════════════════════════════════")
}
```

**Cách fix:**
- ✅ **Clear TOÀN BỘ ImageView** trước khi render
- ✅ Đảm bảo không có ảnh cũ nào còn sót lại
- ✅ Mỗi lần render là một lần fresh start

## Kết quả

### Hành vi mới:
1. **Khi có duplicate `positionNavigation`:**
   - ✅ Hệ thống log ERROR rõ ràng
   - ✅ Giữ layer đầu tiên (không ghi đè)
   - ✅ Layer sau bị ignore

2. **Khi render layers:**
   - ✅ Clear toàn bộ ImageView trước
   - ✅ Không còn ảnh cũ đè lên ảnh mới
   - ✅ Render đúng theo pathSelectedList

### Log output (ví dụ):
```
⚠️ DUPLICATE positionNavigation=20!
   Layer[20]: posNav=20
   Layer[21]: posNav=20, posCus=22
   → Using FIRST occurrence (Layer[20])

📋 Layer Index Cache built: 25 entries
   posNav=0 → Layer[0] (posCus=0)
   posNav=1 → Layer[1] (posCus=1)
   ...
   posNav=20 → Layer[20] (posCus=17)
   posNav=21 → Layer[22] (posCus=23)  ← Note: Layer[21] skipped
```

## Root Cause (Cần fix ở nguồn)

Vấn đề duplicate `positionNavigation` xuất phát từ:
1. **Dữ liệu JSON bị sai** - có 2 layers cùng positionNavigation
2. **API trả về dữ liệu duplicate**
3. **Logic parse JSON** tạo ra duplicate

**Khuyến nghị:**
- Kiểm tra và sửa dữ liệu JSON gốc
- Thêm validation khi parse JSON để reject duplicate
- Đảm bảo `positionNavigation` là **unique identifier**

## Các file đã sửa
1. `CustomizeViewModel.kt`:
   - `buildLayerIndexCache()`: Thêm detection và log duplicate

2. `CustomizeActivity.kt`:
   - `renderAllLayers()`: Clear tất cả ImageView trước khi render

## Testing
Cần test các trường hợp:
1. ✅ Dữ liệu bình thường (không duplicate) → hoạt động như cũ
2. ✅ Dữ liệu có duplicate positionNavigation → log ERROR và dùng layer đầu tiên
3. ✅ Switch giữa các tabs → không còn ảnh cũ đè lên
4. ✅ Random all → clear và render lại đúng
5. ✅ Chọn item khác → clear và render lại đúng

## Ngày fix
31/10/2025

