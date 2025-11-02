# Fix: Duplicate positionCustom Issue

## 🔴 Vấn đề

### Data từ JSON có 2 layers cùng positionCustom:

```
Layer 0 (Body):
  positionCustom: 1
  positionNavigation: 0

Layer 2 (Ears):
  positionCustom: 1  ⚠️ TRÙNG!
  positionNavigation: 2
```

### Hậu quả:

Khi apply suggestion preset, code cũ tìm layer theo `positionCustom`:

```kotlin
// ❌ CODE CŨ - SAI
val layerIndex = _dataCustomize.value?.layerList?.indexOfFirst {
    it.positionCustom == targetPositionCustom  // Tìm layer đầu tiên có positionCustom=1
} ?: return@forEach
```

**Kết quả:**
- Key=-1 (Body) → Tìm thấy Layer 0 (Body) ✅
- Key=1 (Ears) → Tìm thấy Layer 0 (Body) ❌ **SAI!** (Phải tìm Layer 2)
- Cả 2 đều set vào `pathSelectedList[1]` → **Ears ghi đè Body**

## ✅ Giải pháp

### Tìm layer theo logic thông minh:

```kotlin
// ✅ CODE MỚI - ĐÚNG
val layer = if (storageKey == -1) {
    // Key=-1 → Tìm body layer (positionNavigation=0)
    _dataCustomize.value?.layerList?.find { it.positionNavigation == 0 }
} else {
    // Key khác → Tìm layer có positionCustom = storageKey
    val candidateLayers = _dataCustomize.value?.layerList?.filter { 
        it.positionCustom == storageKey 
    }
    
    // Nếu có nhiều layers cùng positionCustom, chọn layer KHÔNG phải body
    if (candidateLayers.size > 1) {
        candidateLayers.find { it.positionNavigation != 0 }  // Chọn Ears, không phải Body
    } else {
        candidateLayers.first()
    }
}
```

## 🎯 Kết quả sau khi fix:

### Xử lý Key=-1 (Body):
```
storageKey = -1
→ Tìm layer có positionNavigation=0
→ Tìm thấy Layer 0 (Body)
→ Set pathSelectedList[1] = body_path ✅
→ Set keySelectedItemList[0] = body_path ✅
```

### Xử lý Key=1 (Ears):
```
storageKey = 1
→ Tìm layers có positionCustom=1
→ Tìm thấy 2 layers: Layer 0 (Body) và Layer 2 (Ears)
→ Chọn layer KHÔNG phải body (positionNav != 0)
→ Chọn Layer 2 (Ears) ✅
→ Set pathSelectedList[1] = ears_path ⚠️ (vẫn ghi đè, nhưng đúng layer)
→ Set keySelectedItemList[2] = ears_path ✅
```

## ⚠️ Lưu ý quan trọng:

### 1. pathSelectedList vẫn bị ghi đè:
```
pathSelectedList[1] = body_path  (từ key=-1)
pathSelectedList[1] = ears_path  (từ key=1) ← GHI ĐÈ
```

**Tại sao?** Vì cả Body và Ears đều có `positionCustom=1`, nên cả 2 đều vẽ vào **cùng 1 ImageView**.

### 2. keySelectedItemList KHÔNG bị ghi đè:
```
keySelectedItemList[0] = body_path   (positionNav=0)
keySelectedItemList[2] = ears_path   (positionNav=2)
```

**Tại sao?** Vì Body và Ears có `positionNavigation` khác nhau (0 và 2).

### 3. Render cuối cùng:
```
ImageView[1] sẽ hiển thị: ears_path (layer cuối cùng được set)
```

## 🔍 Log để verify:

### Log khi apply preset:

```
--- Processing storageKey=-1 ---
✅ Found body layer: positionNav=0, positionCustom=1
Found layer: index=0, positionNav=0, positionCustom=1
Set pathSelectedList[1] = body_path
Set keySelectedItemList[0] = body_path
✅ Applied layer storageKey=-1 → positionCustom=1, positionNav=0

--- Processing storageKey=1 ---
⚠️ Multiple layers with positionCustom=1, choosing non-body layer: positionNav=2
Found layer: index=2, positionNav=2, positionCustom=1
Set pathSelectedList[1] = ears_path  ⚠️ OVERWRITE!
Set keySelectedItemList[2] = ears_path
✅ Applied layer storageKey=1 → positionCustom=1, positionNav=2
```

## 💡 Tại sao không fix hoàn toàn?

### Vấn đề gốc là từ **data structure** (JSON từ server):

Lý tưởng nhất là mỗi layer phải có `positionCustom` **duy nhất**:

```
Layer 0 (Body):   positionCustom: 1, positionNavigation: 0
Layer 1 (Eyes):   positionCustom: 2, positionNavigation: 1
Layer 2 (Ears):   positionCustom: 3, positionNavigation: 2  ← Phải khác 1!
```

Nhưng hiện tại data có:
```
Layer 0 (Body):   positionCustom: 1, positionNavigation: 0
Layer 2 (Ears):   positionCustom: 1, positionNavigation: 2  ← Trùng!
```

### Fix hiện tại:

✅ **Đảm bảo đúng layer được chọn** (Body vs Ears)
✅ **keySelectedItemList đúng** (theo positionNavigation)
⚠️ **pathSelectedList vẫn ghi đè** (do data structure)

### Fix hoàn toàn cần:

1. **Server sửa data**: Đảm bảo mỗi layer có `positionCustom` duy nhất
2. **Hoặc app sửa logic render**: Không dùng `positionCustom` làm key cho ImageView

## 🎯 Kết luận:

Fix này đảm bảo:
- ✅ Đúng layer được chọn khi apply preset
- ✅ UI hiển thị đúng (bottom navigation, color picker)
- ✅ Không bị lỗi khi user click vào tab Body hoặc Ears
- ⚠️ Nhưng vẫn có risk nếu cần render cả Body và Ears cùng lúc

**Recommendation:** Yêu cầu server sửa data để mỗi layer có `positionCustom` duy nhất.

