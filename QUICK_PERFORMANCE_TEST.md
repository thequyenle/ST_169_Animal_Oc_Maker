# 🧪 QUICK PERFORMANCE TEST GUIDE

## 📱 Hướng Dẫn Test Performance Sau Khi Optimize

---

## 🎯 MỤC ĐÍCH TEST

Kiểm tra xem các optimizations đã áp dụng có cải thiện performance thực tế hay không, đặc biệt trên **thiết bị yếu**.

---

## 📋 CHUẨN BỊ

### 1. **Enable Developer Options**
```
Settings → About Phone → Tap "Build Number" 7 lần
```

### 2. **Enable USB Debugging**
```
Settings → Developer Options → USB Debugging (ON)
```

### 3. **Connect Device & Verify**
```cmd
adb devices
```
Phải thấy device trong list.

### 4. **Clear App Data (Optional)**
```cmd
adb shell pm clear com.example.st169_animal_oc_maker
```
Để test "cold start" với cache rỗng.

---

## 🔍 TEST 1: LOAD TIME MEASUREMENT

### **Mục tiêu:** 
Đo thời gian load Character 0 từ lúc chọn đến khi hiển thị đầy đủ.

### **Steps:**
1. Mở app
2. Chọn Character 0
3. **Quan sát logcat:**

```cmd
adb logcat -s Performance:* CustomizeViewModel:* AssetHelper:*
```

### **Kết quả mong đợi:**
```
Performance: ════════════════════════════════════════
Performance: 📊 CHARACTER 0 LOAD COMPLETE
Performance: ⏱️  Total time: 800-2500ms (tùy máy)
Performance: 📱 Device: [Tên máy]
Performance: 💾 RAM: [X.XGB total, X.XGB avail]
Performance: ════════════════════════════════════════
```

### **Benchmark:**
- ✅ **Excellent**: <1000ms
- ✅ **Good**: 1000-2000ms
- ⚠️ **Acceptable**: 2000-3000ms
- ❌ **Poor**: >3000ms (sẽ có warning log)

---

## 🔍 TEST 2: MEMORY USAGE CHECK

### **Mục tiêu:**
Kiểm tra memory usage trong quá trình sử dụng app.

### **Steps:**
1. **Before Opening Character:**
```cmd
adb shell dumpsys meminfo com.example.st169_animal_oc_maker | findstr "TOTAL"
```

2. **After Loading Character 0:**
```cmd
adb shell dumpsys meminfo com.example.st169_animal_oc_maker | findstr "TOTAL"
```

3. **After Random All (nhiều lần):**
```cmd
adb shell dumpsys meminfo com.example.st169_animal_oc_maker | findstr "TOTAL"
```

### **Kết quả mong đợi:**
```
TOTAL PSS:
- Before: 30-50 MB
- After Load: 50-80 MB
- After Random: 60-90 MB (không tăng liên tục)
```

### **Benchmark:**
- ✅ **Good**: Memory ổn định, không tăng liên tục
- ⚠️ **Warning**: Memory tăng dần (memory leak?)
- ❌ **Bad**: Memory > 150MB hoặc OOM crash

---

## 🔍 TEST 3: CACHE HIT VERIFICATION

### **Mục tiêu:**
Kiểm tra xem assets list cache có hoạt động không.

### **Steps:**
1. Load Character 0 (lần đầu)
2. Switch sang Character 1
3. Switch về Character 0 (lần 2)

### **Quan sát logcat:**
```cmd
adb logcat -s AssetHelper:*
```

### **Kết quả mong đợi:**
```
Lần 1: "📦 Cached assets list for: data/data1 (15 items)"
Lần 2: "✅ Cache hit for: data/data1"
```

### **Verification:**
- ✅ Thấy "Cache hit" → Cache hoạt động
- ❌ Không thấy "Cache hit" → Cache không hoạt động

---

## 🔍 TEST 4: GLIDE CONFIGURATION CHECK

### **Mục tiêu:**
Xác nhận MyGlideModule được load và config đúng.

### **Steps:**
1. Mở app lần đầu

### **Quan sát logcat:**
```cmd
adb logcat -s MyGlideModule:*
```

### **Kết quả mong đợi:**
```
MyGlideModule: Glide configured: RAM=X.XGB, isLowRam=true/false, memCache=15/30MB
```

### **Verification:**
- ✅ Thấy log → MyGlideModule đã load
- ✅ `isLowRam=true` cho máy ≤2.5GB RAM hoặc Android ≤8.1
- ✅ `memCache=15MB` cho low-end, `30MB` cho normal
- ❌ Không thấy log → MyGlideModule không được compile

---

## 🔍 TEST 5: LOW MEMORY HANDLING

### **Mục tiêu:**
Kiểm tra xem app có handle low memory tốt không.

### **Steps:**
1. Load Character 0
2. **Simulate low memory:**
```cmd
adb shell am send-trim-memory com.example.st169_animal_oc_maker RUNNING_LOW
```

### **Quan sát logcat:**
```cmd
adb logcat -s Performance:*
```

### **Kết quả mong đợi:**
```
Performance: ⚠️ LOW MEMORY DETECTED (level=10)
Performance:    RAM: X.XGB total, X.XGB avail
Performance:    Clearing Glide memory cache...
Performance:    ✅ Glide memory cache cleared
```

### **Verification:**
- ✅ App không crash
- ✅ Thấy log clear cache
- ✅ Memory usage giảm sau khi clear

---

## 🔍 TEST 6: IMAGE SIZE LIMITING CHECK

### **Mục tiêu:**
Xác nhận ảnh được decode với size giới hạn (512x512).

### **Steps:**
1. Load Character 0
2. Quan sát log khi render layers

### **Quan sát logcat:**
```cmd
adb logcat -s CustomizeActivity:*
```

### **Tìm log:**
```
CustomizeActivity: ✓ GLIDE SUCCESS: 512x512
```

### **Verification:**
- ✅ Tất cả ảnh đều 512x512 hoặc nhỏ hơn
- ❌ Có ảnh > 512x512 → override không hoạt động

---

## 📊 PERFORMANCE COMPARISON

### **Cách So Sánh:**

#### **1. Checkout về commit TRƯỚC optimization:**
```cmd
git log --oneline -10
git checkout [commit_hash_before_optimization]
gradlew.bat clean assembleDebug
```

#### **2. Test và ghi lại metrics:**
- Load time: _____ms
- Memory: _____MB
- Cache hits: _____

#### **3. Checkout về commit SAU optimization:**
```cmd
git checkout main
gradlew.bat clean assembleDebug
```

#### **4. Test lại và so sánh:**
- Load time: _____ms (improvement: ____%)
- Memory: _____MB (reduction: ____%)
- Cache hits: _____ (increase: ____%)

---

## 🎯 QUICK CHECKLIST

### **Trước khi test:**
- [ ] Build success (no errors)
- [ ] Install app trên device
- [ ] Clear app data (optional, for cold start test)
- [ ] Enable logcat filtering

### **Trong khi test:**
- [ ] Load time < 3000ms
- [ ] Memory usage ổn định
- [ ] Cache hits xuất hiện lần 2+
- [ ] MyGlideModule log xuất hiện
- [ ] No crash/ANR

### **Sau khi test:**
- [ ] So sánh với baseline (nếu có)
- [ ] Test trên ít nhất 2 devices (low-end + mid-range)
- [ ] Document kết quả

---

## 🚨 TROUBLESHOOTING

### **Problem: Không thấy log MyGlideModule**

**Possible causes:**
- `kotlin-kapt` plugin chưa apply
- `glide:compiler` chưa add vào dependencies
- Build chưa success

**Solution:**
```cmd
gradlew.bat clean
gradlew.bat assembleDebug --info | findstr Glide
```

---

### **Problem: Cache không hoạt động**

**Possible causes:**
- AssetHelper code chưa được apply
- Cache bị clear mỗi lần restart

**Solution:**
Check log kỹ, nếu thấy "Cached assets list" lần đầu là OK.

---

### **Problem: Load time vẫn chậm**

**Possible causes:**
- Thiết bị quá yếu (RAM < 1GB)
- Nhiều app chạy background
- Storage speed chậm (eMMC cũ)

**Solution:**
- Restart device
- Close background apps
- Test trên device khác để so sánh

---

### **Problem: Memory leak detected**

**Possible causes:**
- Glide không clear properly
- Image view references không release

**Solution:**
Check `onDestroy()` có gọi `Glide.get(this).clearMemory()`.

---

## 📝 TEST RESULT TEMPLATE

```
==========================================
PERFORMANCE TEST RESULTS
==========================================
Date: _______________
Device: _______________
Android: _______________
RAM: _______________

TEST 1 - Load Time:
✅/❌ Character 0 load: _____ms
✅/❌ Character 1 load: _____ms
✅/❌ Character 2 load: _____ms

TEST 2 - Memory Usage:
✅/❌ Before load: _____MB
✅/❌ After load: _____MB
✅/❌ After random: _____MB

TEST 3 - Cache Hits:
✅/❌ First load: No cache
✅/❌ Second load: Cache hit

TEST 4 - Glide Config:
✅/❌ MyGlideModule loaded
✅/❌ isLowRam: _____
✅/❌ memCache: _____MB

TEST 5 - Low Memory:
✅/❌ Handle properly
✅/❌ Cache cleared

TEST 6 - Image Size:
✅/❌ All images ≤512x512

OVERALL:
✅/❌ Pass/Fail
Notes: _______________
==========================================
```

---

## 🎉 SUCCESS CRITERIA

### **Minimum Requirements:**
- ✅ Load time < 3000ms (trên máy RAM 2GB)
- ✅ Memory stable (không tăng liên tục)
- ✅ Cache hits xuất hiện
- ✅ No crash/ANR

### **Optimal Results:**
- 🏆 Load time < 1500ms
- 🏆 Memory < 80MB
- 🏆 Cache hit rate > 80%
- 🏆 Low memory handled gracefully

---

**Version:** 1.0  
**Last Updated:** November 2, 2025  
**Author:** GitHub Copilot

