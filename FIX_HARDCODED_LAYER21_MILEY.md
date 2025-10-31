# Fix Cứng: Layer[21] Character 1 & 2 (Miley & Dammy) posNav=21

## Vấn đề
Trong log Character 1 (Miley) và Character 2 (Dammy) xuất hiện:
```
Layer[20]: posNav=20, posCus=17, path=EMPTY   → CLEAR ImageView[17]
Layer[21]: posNav=20, posCus=22, path=EMPTY   → CLEAR ImageView[22]
```

**Phân tích:**
- Layer[21] có `posNav=20` trong JSON data (đúng theo data)
- Nhưng logic code mong đợi Layer[21] có `posNav=21`
- Khi code request `posNav=21`, không tìm thấy → lỗi!

## Giải pháp: Fix Cứng cho Character 1 & 2

### 1. CustomizeViewModel.kt - `buildLayerIndexCache()`

**Thêm hardfix khi build cache:**
```kotlin
private fun buildLayerIndexCache() {
    val layerList = _dataCustomize.value?.layerList ?: return
    val cache = mutableMapOf<Int, Int>()

    layerList.forEachIndexed { index, layer ->
        // 🔧 HARDFIX: Character 1 & 2, Layer[21] có posNav=20 trong data
        // → Bỏ qua duplicate warning và thêm vào cache với posNav=21
        if ((positionSelected == 1 || positionSelected == 2) && index == 21 && layer.positionNavigation == 20) {
            cache[21] = index  // Thêm vào cache với key=21
            Log.d("CustomizeViewModel", "🔧 HARDFIX Character $positionSelected: Layer[21] mapped to posNav=21")
            return@forEachIndexed
        }
        
        // ...existing duplicate detection...
    }
    
    _layerIndexCache.value = cache
    
    // Log cache for debugging
    Log.d("CustomizeViewModel", "📋 Layer Index Cache built: ${cache.size} entries (Character $positionSelected)")
}
```

**Cách hoạt động:**
- ✅ Khi là Character 1 hoặc 2 (positionSelected == 1 || positionSelected == 2)
- ✅ Và Layer[21] (index == 21)
- ✅ Và layer có posNav=20 (layer.positionNavigation == 20)
- ✅ → Thêm vào cache với **key=21** thay vì key=20
- ✅ → Không báo duplicate warning

### 2. CustomizeViewModel.kt - `getPathIndexForLayer()`

**Thêm hardfix khi get pathIndex:**
```kotlin
fun getPathIndexForLayer(positionNavigation: Int): Int {
    val cache = _layerIndexCache.value
    val layerList = _dataCustomize.value?.layerList ?: return 0

    // 🎯 FIX CỨNG: Đối với Character 1 & 2 (Miley & Dammy), Layer[21] có posNav=20 trong data
    // → Fix cứng: khi request posNav=21, trả về Layer[21]
    if ((positionSelected == 1 || positionSelected == 2) && positionNavigation == 21) {
        // Tìm Layer[21] (index 21 trong layerList)
        if (layerList.size > 21) {
            Log.d("CustomizeViewModel", "🔧 HARDFIX Character $positionSelected: posNav=21 → Layer[21]")
            return 21  // Trả về layerIndex = 21
        } else {
            Log.e("CustomizeViewModel", "❌ HARDFIX failed: Layer[21] not found")
            return -1
        }
    }

    // ...existing cache lookup logic...
}
```

**Cách hoạt động:**
- ✅ Khi là Character 1 hoặc 2 (positionSelected == 1 || positionSelected == 2)
- ✅ Và code request `posNav=21`
- ✅ → Trả về trực tiếp `layerIndex = 21`
- ✅ → Không cần lookup cache

## Kết quả

### Trước khi fix:
```
Cache:
  posNav=20 → Layer[20] ❌ (Layer[21] bị duplicate, không vào cache)
  
getPathIndexForLayer(21):
  → Không tìm thấy trong cache → return -1 ❌
```

### Sau khi fix:
```
Cache:
  posNav=20 → Layer[20] ✅
  posNav=21 → Layer[21] ✅ (hardfix)
  
getPathIndexForLayer(21):
  → Character 1 detected → return 21 ✅ (hardfix)
  → Hoặc lookup cache → Layer[21] ✅
```

### Log output (ví dụ Character 1):
```
🔧 HARDFIX Character 1: Layer[21] mapped to posNav=21 (actual posNav=20, posCus=22)

📋 Layer Index Cache built: 26 entries (Character 1)
   posNav=0 → Layer[0] (posCus=0)
   posNav=1 → Layer[1] (posCus=1)
   ...
   posNav=20 → Layer[20] (posCus=17)
   posNav=21 → Layer[21] (posCus=22) ← HARDFIX
   posNav=22 → Layer[22] (posCus=23)
```

### Log output (ví dụ Character 2):
```
🔧 HARDFIX Character 2: Layer[21] mapped to posNav=21 (actual posNav=20, posCus=0)

📋 Layer Index Cache built: 31 entries (Character 2)
   posNav=0 → Layer[0] (posCus=4)
   posNav=1 → Layer[1] (posCus=5)
   ...
   posNav=20 → Layer[20] (posCus=27)
   posNav=21 → Layer[21] (posCus=0) ← HARDFIX
   posNav=22 → Layer[22] (posCus=1)
```

## Scope của Fix

**CHỈ áp dụng cho Character 1 & 2 (Miley & Dammy):**
- ✅ `positionSelected == 1` hoặc `positionSelected == 2`
- ✅ Layer[21]
- ✅ posNav mapping: 20 → 21

**KHÔNG ảnh hưởng:**
- ❌ Character 0 (positionSelected == 0)
- ❌ Các character khác
- ❌ Các layer khác của Character 1 & 2

## Note quan trọng

### Tại sao không sửa JSON?
Vì data JSON đúng theo thiết kế (Layer[21] có posNav=20). Vấn đề là logic code mong đợi khác → Fix ở code.

### Khi nào cần sửa lại?
- Nếu JSON được update: Layer[21] có posNav=21
- → Xóa hardfix này đi
- → Code sẽ hoạt động bình thường với cache

## Các file đã sửa
1. `CustomizeViewModel.kt`:
   - `buildLayerIndexCache()`: Thêm hardfix mapping posNav=21 cho Layer[21]
   - `getPathIndexForLayer()`: Thêm hardfix return 21 khi request posNav=21

## Testing
Test cases cho Character 1 & 2:
1. ✅ Click vào tab có posNav=21 → Layer[21] render đúng (cả char 1 và 2)
2. ✅ Select màu cho posNav=21 → màu apply đúng (cả char 1 và 2)
3. ✅ Random all → Layer[21] được random đúng (cả char 1 và 2)
4. ✅ Cache build đúng với posNav=21 → Layer[21] (cả char 1 và 2)
5. ✅ Character 0 không bị ảnh hưởng
6. ✅ pathSelectedList[21] được lưu đúng khi click item ở posNav=21

## Ngày fix
31/01/2025

