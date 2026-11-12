# TeamUp Backend API (Node.js + Express + MongoDB Atlas)

Backend authentication service for the TeamUp Platform.

---

## 🚀 Quick Start (কীভাবে চালু করবেন)

### 1. Requirements (প্রয়োজনীয় রিকোয়ারমেন্ট)
- **Node.js**: v18+ (v20+ recommended)
- **MongoDB Atlas** Account & Cluster connection string

---

### 2. Configuration (কনফিগারেশন)

`server/.env` ফাইলে আপনার MongoDB Atlas এর কানেকশন স্ট্রিং দিন:

```env
PORT=5000
MONGODB_URI=mongodb+srv://<username>:<password>@cluster0.mongodb.net/teamup?retryWrites=true&w=majority
JWT_SECRET=teamup_jwt_production_secret_key_2026_super_secure
JWT_EXPIRES_IN=30d
```

> **টিপস:** `<username>` ও `<password>` আপনার MongoDB Atlas Database User এর ইউজারনেম ও পাসওয়ার্ড দিয়ে রিপ্লেস করুন।

---

### 3. Start Server (সার্ভার রান করার নিয়ম)

```bash
# সার্ভার ডিরেক্টরিতে যান
cd server

# প্রোডাকশন মোডে চালু করতে
npm start

# অথবা ডেভেলপমেন্ট মোডে অটো-রিলোড সহ চালু করতে
npm run dev
```

সার্ভার সফলভাবে চালু হলে কনসোলে দেখতে পাবেন:
```text
====================================================
 TeamUp Node.js API Server running on port 5000
 Health check: http://localhost:5000/api/health
 Auth endpoints:
   POST http://localhost:5000/api/auth/register
   POST http://localhost:5000/api/auth/login
====================================================
```

---

## 🛡️ Admin Role Assignment (ডাটাবেস থেকে ম্যানুয়ালি অ্যাডমিন করার নিয়ম)

আপনার রিকোয়ারমেন্ট অনুযায়ী: **অ্যাডমিন রোল রেজিস্ট্রেশন ফর্ম থেকে সরাসরি সিলেক্ট করা যাবে না**। সকল রেজিস্ট্রেশন ডিফল্টভাবে `Student` হিসেবে তৈরি হবে। অ্যাডমিন রোল শুধুমাত্র ডাটাবেস থেকে ম্যানুয়ালি সেট করা যাবে:

### ধাপসমূহ (MongoDB Atlas Dashboard):
1. **[cloud.mongodb.com](https://cloud.mongodb.com)**-এ লগইন করুন।
2. আপনার ক্লাস্টারের **Browse Collections**-এ যান।
3. **`teamup`** ডাটাবেস ওপেন করে **`users`** কালেকশন সিলেক্ট করুন।
4. যে ইউজারকে অ্যাডমিন বানাতে চান তার ডকুমেন্টটি খুঁজুন (ইমেইল দিয়ে সার্চ করতে পারেন)।
5. ডকুমেন্টের `role` ফিল্ডের পেন্সিল আইকনে ক্লিক করে মানটি `"Admin"` লিখে **Update** বাটনে ক্লিক করুন।
6. ব্যাস! এখন এই ইউজার যখন TeamUp অ্যাপে লগইন করবে, সিস্টেম স্বয়ংক্রিয়ভাবে তাকে সনাক্ত করে **Admin Dashboard** ওপেন করে দেবে।

---

## 📡 API Endpoints Summary

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/health` | সার্ভার এবং ডাটাবেস স্ট্যাটাস চেক | Public |
| `POST` | `/api/auth/register` | নতুন ইউজার রেজিস্ট্রেশন (ডিফল্ট: Student) | Public |
| `POST` | `/api/auth/login` | ইউজার লগইন ও JWT টোকেন ইস্যু | Public |
| `GET` | `/api/auth/me` | বর্তমান লগইন করা ইউজারের প্রোফাইল | Private (JWT Bearer) |
| `GET` | `/api/auth/users` | সমস্ত রেজিস্ট্রেশন করা ইউজারের লিস্ট | Private / Admin |

---

## 🔄 Client Fallback (অফলাইন সাপোর্ট)
TeamUp Java ক্লায়েন্ট (`AuthStore.java`) স্বয়ংক্রিয়ভাবে `http://localhost:5000/api` কল করে। যদি কোনো কারণে ব্যাকএন্ড সার্ভার সাময়িকভাবে বন্ধ থাকে, ক্লায়েন্ট অ্যাপ ক্র্যাশ না করে স্বয়ংক্রিয়ভাবে লোকাল স্টোরেজে (`data/users.csv`) PBKDF2 হ্যাশিং সহ কাজ চালিয়ে যাবে।

