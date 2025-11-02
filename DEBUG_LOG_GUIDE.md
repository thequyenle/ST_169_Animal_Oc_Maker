# 🔍 Debug Log Guide: Duplicate Tail Issue

## 📋 Logs Added

Đã thêm log chi tiết vào các điểm quan trọng để debug vấn đề 2 đuôi cùng hiển thị.

## 🎯 Test Scenario

### Scenario 1: Random All
1. Mở Character 1 (có 2 đuôi ở positionCustom=21)
2. Click **"Random All"**
3. Xem log trong Logcat

**Expected Log:**
```
🎲 RANDOM ALL CLICKED
pathSelectedList after Random All:
  [0] posNav=0, posCus=1, path=body_xxx.png
  [1] posNav=1, posCus=2, path=eyes_xxx.png
  ...
  [18] posNav=18, posCus=21, path=tail_A_xxx.png    ← Đuôi A
  [21] = EMPTY                                        ← Đuôi B bị skip (vì cùng positionCustom)

🎨 RENDER ALL LAYERS START
Layer[0]: posNav=0, posCus=1, path=body_xxx.png
  → RENDER to BODY ImageView
Layer[1]: posNav=1, posCus=2, path=eyes_xxx.png
  → RENDER to ImageView[2]
...
Layer[18]: posNav=18, posCus=21, path=tail_A_xxx.png
  → RENDER to ImageView[21]
Layer[21]: posNav=21, posCus=21, path=EMPTY
  → CLEAR ImageView[21]                              ← Clear đuôi B
🎨 RENDER ALL LAYERS END
```

**Nếu vẫn thấy 2 đuôi:**
```
❌ BUG: Layer[18] và Layer[21] đều có path (không EMPTY)
→ Nghĩa là fix trong ViewModel.setClickRandomFullLayer() chưa work
```

---

### Scenario 2: Random All → Chọn Đuôi Khác

1. Click **"Random All"** → Random được Đuôi A
2. Chọn tab **navigation 21** (Đuôi B)
3. Click chọn một item Đuôi B
4. Xem log

**Expected Log:**
```
👆 USER CLICKED ITEM
Item: tail_B_xxx.png
Position in RCV: 3
positionCustom: 21
positionNavSelected: 21

🧹 CLEAR DUPLICATE: Cleared layer positionNav=18 (same positionCustom=21)  ← ViewModel log

✅ pathSelected: tail_B_xxx.png
pathSelectedList after click:
  [0] = body_xxx.png
  ...
  [18] = EMPTY                                        ← Đuôi A đã bị xóa
  ...
  [21] = tail_B_xxx.png                              ← Đuôi B mới được set

🎨 RENDER ALL LAYERS START
Layer[18]: posNav=18, posCus=21, path=EMPTY
  → CLEAR ImageView[21]                              ← Clear đuôi A
Layer[21]: posNav=21, posCus=21, path=tail_B_xxx.png
  → RENDER to ImageView[21]                          ← Render đuôi B
🎨 RENDER ALL LAYERS END
```

**Nếu vẫn thấy 2 đuôi:**
```
❌ BUG: Layer[18] vẫn có path (không EMPTY)
→ Nghĩa là clearLayersWithSamePositionCustom() chưa work
→ Hoặc render logic sai
```

---

## 🔍 Key Points to Check

### 1. pathSelectedList State
- ✅ **Đúng:** Chỉ 1 trong 2 layers (18 hoặc 21) có path, layer còn lại EMPTY
- ❌ **Sai:** Cả 2 layers đều có path

### 2. Render Logic
- ✅ **Đúng:** Layer có path EMPTY được CLEAR
- ❌ **Sai:** Layer có path EMPTY vẫn được RENDER (bị cache?)

### 3. ImageView Mapping
```
Layer[18]: positionCustom=21 → Render to ImageView[21]
Layer[21]: positionCustom=21 → Render to ImageView[21]
```
- ✅ **Đúng:** Cả 2 cùng ImageView, nên chỉ 1 được hiển thị (cái sau ghi đè cái trước)
- ❌ **Sai:** Nếu render theo thứ tự khác (18 sau 21) thì sẽ hiển thị sai layer

---

## 🐛 Possible Issues

### Issue 1: ViewModel Logic Failed
**Symptom:**
```
pathSelectedList after Random All:
  [18] = tail_A_xxx.png
  [21] = tail_B_xxx.png    ← BUG: Không được phép có cả 2
```

**Solution:** Check `setClickRandomFullLayer()` trong ViewModel
- Verify `positionCustomMap` có track đúng không
- Verify logic skip có chạy không

### Issue 2: Clear Logic Failed
**Symptom:**
```
👆 USER CLICKED ITEM (Đuôi B)
pathSelectedList after click:
  [18] = tail_A_xxx.png    ← BUG: Phải là EMPTY
  [21] = tail_B_xxx.png
```

**Solution:** Check `clearLayersWithSamePositionCustom()` trong ViewModel
- Verify có được gọi không
- Verify tìm đúng layer cần xóa không

### Issue 3: Render Order Wrong
**Symptom:**
```
🎨 RENDER ALL LAYERS START
Layer[21]: ... → RENDER to ImageView[21]  (tail_B)
Layer[18]: ... → RENDER to ImageView[21]  (tail_A)  ← Ghi đè tail_B!
```

**Solution:** Render theo đúng thứ tự index (0→N), không shuffle

### Issue 4: Glide Cache
**Symptom:**
- pathSelectedList đúng (1 EMPTY, 1 có path)
- Log render đúng (CLEAR và RENDER đúng)
- Nhưng UI vẫn hiển thị cả 2 đuôi

**Solution:** 
```kotlin
// Clear image trước khi load mới
Glide.with(context).clear(imageView)
// Hoặc disable cache
.diskCacheStrategy(DiskCacheStrategy.NONE)
.skipMemoryCache(true)
```

---

## 📱 How to View Logs

### Android Studio Logcat:
1. Mở **Logcat** tab (bottom)
2. Filter: `CustomizeActivity` hoặc `CustomizeViewModel`
3. Tìm các log bắt đầu bằng:
   - `🎲 RANDOM ALL CLICKED`
   - `👆 USER CLICKED ITEM`
   - `🎨 RENDER ALL LAYERS`
   - `🧹 CLEAR DUPLICATE`
   - `🧹 RANDOM ALL SKIP`

### Via adb:
```bash
adb logcat -s CustomizeActivity:D CustomizeViewModel:D
```

---

## ✅ Success Criteria

Khi fix thành công, log phải như sau:

**Random All:**
```
✅ RANDOM ALL SET: positionNav=18 (positionCustom=21)
🧹 RANDOM ALL SKIP: positionNav=21 (positionCustom=21 already assigned)
```

**Manual Select:**
```
🧹 CLEAR DUPLICATE: Cleared layer positionNav=18 (same positionCustom=21)
```

**Render:**
```
Layer[18]: posNav=18, posCus=21, path=EMPTY → CLEAR ImageView[21]
Layer[21]: posNav=21, posCus=21, path=tail_B.png → RENDER to ImageView[21]
```

**Kết quả:** Chỉ 1 đuôi hiển thị trên màn hình ✅

---

## 📊 Report Back

Sau khi test, hãy report lại:

1. **Log của Random All** (copy từ Logcat)
2. **Log của Click item** (copy từ Logcat)
3. **Screenshot** của UI (có 1 hay 2 đuôi?)
4. **Description:** Mô tả ngắn gọn điều bạn thấy

Với log chi tiết này, tôi sẽ biết chính xác vấn đề nằm ở đâu! 🎯

