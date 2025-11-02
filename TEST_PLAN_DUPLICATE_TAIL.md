# 🧪 Test Plan: Duplicate Tail Fix

## ✅ Build Status
**BUILD SUCCESSFUL** ✓

## 📋 Test Checklist

### 🎲 Test Group 1: Random Actions

#### Test 1.1: Random All (Basic)
- [ ] 1. Mở Character 1 (có 2 đuôi cùng positionCustom=21)
- [ ] 2. Click "Random All"
- [ ] 3. **Verify:** Chỉ có 1 đuôi hiển thị (không duplicate)
- [ ] 4. Note log: `✅ RANDOM ALL SET` cho 1 đuôi, `🧹 RANDOM ALL SKIP` cho đuôi kia

#### Test 1.2: Random All (Multiple times)
- [ ] 1. Click "Random All" 5 lần
- [ ] 2. **Verify:** Mỗi lần chỉ có 1 đuôi hiển thị
- [ ] 3. **Verify:** Có thể random được cả Đuôi A và Đuôi B (không fix ở cùng 1 đuôi)

#### Test 1.3: Random Single Tab
- [ ] 1. Chọn tab navigation 18 (Đuôi A)
- [ ] 2. Click "Random" trên tab đó
- [ ] 3. Chuyển sang tab 21 (Đuôi B)
- [ ] 4. Click "Random" trên tab đó
- [ ] 5. **Verify:** Chỉ có Đuôi B hiển thị (Đuôi A đã bị xóa)
- [ ] 6. Note log: `🧹 CLEAR DUPLICATE`

---

### 👆 Test Group 2: Manual Selection

#### Test 2.1: Random All → Manual Select
- [ ] 1. Click "Random All" → Random được Đuôi A
- [ ] 2. Chọn tab navigation 21 (Đuôi B)
- [ ] 3. Click chọn Đuôi B (item bất kỳ)
- [ ] 4. **Verify:** Chỉ có Đuôi B hiển thị (Đuôi A đã bị xóa)
- [ ] 5. Note log: `🧹 CLEAR DUPLICATE: Cleared layer positionNav=18`

#### Test 2.2: Manual Select Both Tabs
- [ ] 1. Chọn tab 18 → Chọn Đuôi A (item bất kỳ)
- [ ] 2. **Verify:** Đuôi A hiển thị
- [ ] 3. Chọn tab 21 → Chọn Đuôi B (item bất kỳ)
- [ ] 4. **Verify:** Chỉ Đuôi B hiển thị (Đuôi A đã bị xóa)

#### Test 2.3: Select with Color Variants
- [ ] 1. Random All → Random được Đuôi A màu đỏ
- [ ] 2. Chọn tab 21 → Chọn Đuôi B màu xanh
- [ ] 3. **Verify:** Chỉ Đuôi B màu xanh hiển thị
- [ ] 4. Đổi màu Đuôi B → màu vàng
- [ ] 5. **Verify:** Vẫn chỉ có Đuôi B (màu vàng)

---

### 🎨 Test Group 3: Suggestion Preset

#### Test 3.1: Apply Preset from Gallery
- [ ] 1. Vào Gallery
- [ ] 2. Click vào 1 suggestion có Đuôi A
- [ ] 3. **Verify:** Apply thành công, chỉ Đuôi A hiển thị
- [ ] 4. Note log: `🧹 PRESET CLEAR DUPLICATE` (nếu có)

#### Test 3.2: Apply Preset → Random
- [ ] 1. Apply suggestion có Đuôi A
- [ ] 2. Click "Random All"
- [ ] 3. **Verify:** Có thể random được Đuôi B, và chỉ Đuôi B hiển thị

#### Test 3.3: Apply Preset → Manual Select
- [ ] 1. Apply suggestion có Đuôi A
- [ ] 2. Chọn tab 21 → Chọn Đuôi B thủ công
- [ ] 3. **Verify:** Chỉ Đuôi B hiển thị

---

### 🔄 Test Group 4: Mixed Scenarios

#### Test 4.1: Complex Mix
- [ ] 1. Random All (random Đuôi A)
- [ ] 2. Chọn tab Eyes → Random
- [ ] 3. Chọn tab 21 (Đuôi B) → Random
- [ ] 4. **Verify:** Chỉ Đuôi B hiển thị
- [ ] 5. Chọn tab Body → Manual select
- [ ] 6. **Verify:** Vẫn chỉ Đuôi B hiển thị (không ảnh hưởng)

#### Test 4.2: Reset Button
- [ ] 1. Random All → Random được Đuôi A
- [ ] 2. Click "Reset"
- [ ] 3. **Verify:** App reset về trạng thái mặc định
- [ ] 4. Chọn tab 21 → Chọn Đuôi B
- [ ] 5. **Verify:** Chỉ Đuôi B hiển thị

#### Test 4.3: Switch Characters
- [ ] 1. Ở Character 1 → Random All (có Đuôi A)
- [ ] 2. Switch sang Character 2
- [ ] 3. **Verify:** Character 2 load bình thường
- [ ] 4. Switch về Character 1
- [ ] 5. **Verify:** Character 1 giữ nguyên state (vẫn có Đuôi A)
- [ ] 6. Chọn tab 21 → Chọn Đuôi B
- [ ] 7. **Verify:** Chỉ Đuôi B hiển thị

---

### 🖼️ Test Group 5: Export & General

#### Test 5.1: Export Image
- [ ] 1. Random All → Random được Đuôi A
- [ ] 2. Chọn tab 21 → Chọn Đuôi B
- [ ] 3. Click "Save" hoặc "Export"
- [ ] 4. Kiểm tra ảnh đã save
- [ ] 5. **Verify:** Ảnh chỉ có Đuôi B (không có duplicate)

#### Test 5.2: Other Layers Not Affected
- [ ] 1. Random All
- [ ] 2. **Verify:** Body, Eyes, Ears, v.v. vẫn hiển thị bình thường
- [ ] 3. Chọn tab Đuôi → Đổi đuôi
- [ ] 4. **Verify:** Các layers khác không bị ảnh hưởng

#### Test 5.3: Performance Check
- [ ] 1. Random All 10 lần liên tục
- [ ] 2. **Verify:** App không lag, không crash
- [ ] 3. Chọn đuôi thủ công 10 lần liên tục (switch giữa 2 tabs)
- [ ] 4. **Verify:** App phản hồi nhanh

---

## 📊 Log Verification

### Expected Logs for Success:

#### When Random All:
```
✅ RANDOM ALL SET: positionNav=18 (positionCustom=21)
🧹 RANDOM ALL SKIP: positionNav=21 (positionCustom=21 already assigned)
```

#### When Manual Select:
```
🧹 CLEAR DUPLICATE: Cleared layer positionNav=18 (same positionCustom=21)
✅ DAMMY SAVED: pathIndex=21
```

#### When Random Single:
```
🧹 CLEAR DUPLICATE: Cleared layer positionNav=18 (same positionCustom=21)
```

#### When Apply Preset:
```
🧹 PRESET CLEAR DUPLICATE: Cleared positionNav=18 (same positionCustom=21)
```

---

## ❌ Bug Report Template

Nếu phát hiện bug, ghi lại theo format:

```
**Test Case:** [Test number, e.g., 2.1]
**Steps:**
1. ...
2. ...

**Expected:** Chỉ 1 đuôi hiển thị
**Actual:** 2 đuôi hiển thị / Crash / Other

**Logs:** (Paste relevant logs)

**Screenshot:** (Attach nếu có)
```

---

## ✅ Sign-off

**Tester:** _________________
**Date:** _________________
**Build Version:** _________________
**Test Result:** ☐ PASS  ☐ FAIL

**Notes:**

