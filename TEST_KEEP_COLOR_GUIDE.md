# 🧪 HƯỚNG DẪN TEST: Giữ màu khi chuyển từ None sang item khác

## ✅ BUILD STATUS
```
BUILD SUCCESSFUL ✅
Date: 2025-11-01
Time: 3m 45s
Warnings: Chỉ có deprecated APIs (không ảnh hưởng)
```

---

## 📋 TEST CASES

### **Test Case 1: Chọn màu ở None, chuyển sang item có đủ màu**

**Steps:**
1. Mở app → Chọn character bất kỳ
2. Vào màn Customize
3. Chọn một layer (ví dụ: Eyes, Mouth, Hair...)
4. Click nút **None** (nút đầu tiên trong rcvLayer)
5. Chọn màu thứ **5** trong rcvColor (scroll xuống chọn màu bất kỳ)
6. Click vào **item có ảnh** bất kỳ trong rcvLayer

**Expected Result:**
```
✅ Item được load ra
✅ rcvColor hiển thị màu thứ 5 được highlight (màu đã chọn ở bước 5)
✅ Character được render với item + màu đã chọn
```

**Before Fix:**
```
❌ rcvColor hiển thị màu 0 hoặc random
❌ Màu đã chọn ở None bị mất
```

---

### **Test Case 2: Chọn màu cao, item mới có ít màu hơn**

**Steps:**
1. Vào màn Customize
2. Chọn layer Eyes (hoặc layer có nhiều items)
3. Click **None**
4. Chọn màu **số 8** hoặc **số 9** (màu cuối)
5. Click vào item chỉ có **3-5 màu**

**Expected Result:**
```
✅ rcvColor hiển thị màu CUỐI CÙNG của item mới được highlight
✅ Ví dụ: Đã chọn màu 8, item mới có 5 màu → màu 4 (index cuối) được chọn
✅ Character được render đúng với màu cuối cùng
```

**Logic:**
```kotlin
// Coerce logic
currentColorIndex = 8
item mới có 5 màu (index 0-4)
safeColorIndex = coerceIn(8, 0, 4) = 4 ✅
```

---

### **Test Case 3: Chọn màu ở None, chuyển layer khác**

**Steps:**
1. Layer **Eyes**: Click None → Chọn màu 3
2. Chuyển sang layer **Mouth** (click tab navigation bottom)
3. Kiểm tra rcvColor của Mouth
4. Chuyển lại layer **Eyes**

**Expected Result:**
```
✅ Layer Mouth: Hiển thị màu đã chọn trước đó của Mouth (không bị ảnh hưởng)
✅ Layer Eyes: Vẫn giữ màu 3 đã chọn ở bước 1
✅ Mỗi layer độc lập, không ảnh hưởng lẫn nhau
```

---

### **Test Case 4: None → Chọn màu → Click item → Đổi màu**

**Steps:**
1. Click **None**
2. Chọn màu **5**
3. Click vào **item A**
4. Chọn màu **7** mới
5. Click vào **item B** khác

**Expected Result:**
```
✅ Item A được load với màu 5 (bước 3)
✅ Sau khi chọn màu 7 → item A hiển thị màu 7
✅ Click item B → item B hiển thị màu 7 (giữ màu vừa chọn)
✅ Màu 7 vẫn được highlight trong rcvColor
```

---

### **Test Case 5: Item A → None → Chọn màu → Item B**

**Steps:**
1. Click vào **Item A** (có 8 màu) → auto chọn màu 0
2. Click **None**
3. Scroll rcvColor, chọn màu **6**
4. Click vào **Item B** (có 10 màu)

**Expected Result:**
```
✅ Item B được load ra
✅ rcvColor hiển thị màu 6 được highlight
✅ Item B render với màu 6
```

---

### **Test Case 6: Random Button**

**Steps:**
1. Click **None**
2. Chọn màu **4**
3. Click nút **Random** (nút thứ 2 trong rcvLayer)
4. Kiểm tra rcvColor

**Expected Result:**
```
✅ Random item được load
✅ Random màu được chọn (có thể khác màu 4)
✅ rcvColor hiển thị màu mới được random
✅ Không bị ảnh hưởng bởi màu đã chọn trước đó
```

---

### **Test Case 7: None → Item không có màu**

**Steps:**
1. Click **None**
2. Chọn màu **5**
3. Click vào item **KHÔNG CÓ màu** (nếu có)

**Expected Result:**
```
✅ Item được load ra
✅ rcvColor trống hoặc disabled (vì item không có màu)
✅ Character render với item mặc định (không có màu)
```

**Code handle:**
```kotlin
if (item.listImageColor.isEmpty()) {
    _positionColorItemList.value[positionNavSelected.value] = 0
    // Không cập nhật colorItemNavList
}
```

---

## 🐛 DEBUG TIPS

### **Nếu màu vẫn bị reset:**

**1. Check log trong Logcat:**
```
Filter: CustomizeViewModel
Tìm: "🎨" hoặc "setClickFillLayer"
```

**Expected logs:**
```
📍 setClickFillLayer:
   positionNavSelected: 1
   pathIndex returned: 3
   ✅ SAVED: pathSelectedList[3] = xxx.png

🎨 Color position: 5
🎨 Updated colorItemNavList for layer 1
```

**2. Breakpoint:**
- File: `CustomizeViewModel.kt`
- Line: 657 (trong `setClickFillLayer`)
- Check: `currentColorIndex`, `safeColorIndex`

**3. Check data:**
```kotlin
// Trong handleFillLayer()
Log.d("TEST", "positionColorItemList: ${viewModel.positionColorItemList.value}")
Log.d("TEST", "colorItemNavList size: ${viewModel.colorItemNavList.value[layer].size}")
Log.d("TEST", "selected color index: ${viewModel.colorItemNavList.value[layer].indexOfFirst { it.isSelected }}")
```

---

## 📊 VERIFICATION CHECKLIST

Sau khi test, kiểm tra:

- [ ] **Test Case 1**: Màu được giữ khi chuyển từ None sang item ✅
- [ ] **Test Case 2**: Coerce đúng khi item mới có ít màu hơn ✅
- [ ] **Test Case 3**: Mỗi layer độc lập, không ảnh hưởng lẫn nhau ✅
- [ ] **Test Case 4**: Đổi màu nhiều lần không bị lỗi ✅
- [ ] **Test Case 5**: Flow phức tạp (A→None→B) hoạt động đúng ✅
- [ ] **Test Case 6**: Random không bị ảnh hưởng ✅
- [ ] **Test Case 7**: Item không màu được handle đúng ✅
- [ ] **Performance**: Không lag khi scroll rcvColor ✅
- [ ] **UI**: rcvColor auto scroll đến màu đã chọn ✅
- [ ] **Memory**: Không memory leak khi switch nhiều lần ✅

---

## 🎯 KẾT QUẢ MONG ĐỢI

### **Trước khi fix:**
```
User: Click None → Chọn màu 5 → Click item khác
Result: ❌ rcvColor hiển thị màu 0
        ❌ Phải chọn lại màu từ đầu
```

### **Sau khi fix:**
```
User: Click None → Chọn màu 5 → Click item khác
Result: ✅ rcvColor vẫn hiển thị màu 5
        ✅ Item được render với màu 5 luôn
        ✅ Không cần chọn lại
```

---

## 📝 GHI CHÚ

1. **Auto scroll**: Khi switch item, rcvColor tự động scroll đến màu đã chọn
2. **Coerce logic**: Nếu chọn màu 8 nhưng item mới chỉ có 5 màu → auto chọn màu 4 (cuối cùng)
3. **Performance**: Giảm 17 dòng code → tăng performance khi rebuild colorList
4. **Consistency**: Tất cả màu đều được lấy từ ViewModel, không tạo mới trong Activity

---

## 🚀 NEXT STEPS

Sau khi test OK:
1. Commit code với message: `fix: Giữ màu đã chọn khi chuyển từ None sang item khác`
2. Push lên Git
3. Update CHANGELOG.md (nếu có)
4. Đóng issue/ticket liên quan

---

**Tester:** _____________  
**Date:** 2025-11-01  
**Status:** ⏳ Pending Test  
**Result:** ⬜ Pass / ⬜ Fail

