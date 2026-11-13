# TeamUp Backend API (Node.js + Express + MongoDB Atlas)

Backend for the TeamUp Platform: authentication + the full Student Dashboard (projects, teammates, tasks, teams, profile, settings).

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
# TeamUp নিজস্ব ডাটাবেসে ডাটা রাখে (একই ক্লাস্টারের অন্য অ্যাপের সাথে মিশে যায় না)
MONGODB_DB_NAME=teamup
```

> **টিপস:** `<username>` ও `<password>` আপনার MongoDB Atlas Database User এর ইউজারনেম ও পাসওয়ার্ড দিয়ে রিপ্লেস করুন।

---

### 3. Demo Data Seed

Dashboard যেন ফাঁকা না থাকে, Java অ্যাপের ডেমো ডাটা (প্রজেক্ট, টিমমেট, টাস্ক, রিভিউ...) ডাটাবেসে পুশ করুন:

```bash
cd server
npm run seed                          # demo data যোগ করে (আগে থেকে থাকলে skip)
npm run seed:reset                    # demo data মুছে আবার নতুন করে তৈরি করে
npm run seed -- --attach=you@mail.com # আপনার অ্যাকাউন্টকে demo টিম ও টাস্কে যুক্ত করে
npm run seed -- --attach-all          # সব non-demo (Admin বাদে) অ্যাকাউন্টের জন্য
npm run seed -- --clean               # শুধু demo data মুছে ফেলে
```

- 28 demo users, 19 projects, 23 tasks, join requests, activity, reviews তৈরি হয়।
- সব demo অ্যাকাউন্টের পাসওয়ার্ড: `teamup123`
- পুরো ডাটাসহ দেখার জন্য লগইন করুন: **`alex.morgan@example.com` / `teamup123`**
- তারিখগুলো seed করার সময় থেকে হিসাব হয়, তাই deadline সবসময় "আজ/কাল" দেখায়।
- Demo data `isDemo: true` দিয়ে চিহ্নিত — reset/clean শুধু এগুলো (আর demo ইউজারদের তৈরি প্রজেক্ট) মোছে, আসল ইউজারদের ডাটা নয়।

---

### 4. Start Server (সার্ভার রান করার নিয়ম)

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
| `GET` | `/api/auth/users` | সমস্ত রেজিস্ট্রেশন করা ইউজারের লিস্ট | Admin (JWT) |

### Student Dashboard (সবগুলোতে `Authorization: Bearer <token>` লাগবে)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/dashboard` | এক কলে পুরো ড্যাশবোর্ড: overview, discover, teammates, my projects, team workspace, profile, settings |
| `POST` | `/api/projects` | নতুন প্রজেক্ট তৈরি (`title, description, category, skills[]`) |
| `POST` | `/api/projects/:id/bookmark` | বুকমার্ক (`{active: true/false}`) |
| `POST` | `/api/projects/:id/join` | প্রজেক্টে জয়েন রিকোয়েস্ট |
| `POST` | `/api/projects/:id/tasks` | টাস্ক যোগ (শুধু টিম মেম্বার) |
| `POST` | `/api/tasks/:id/toggle` | টাস্ক done/undone (`{done: true/false}`) |
| `POST` | `/api/requests/:id/accept` · `/decline` | ক্যান্ডিডেট রিকোয়েস্ট (শুধু প্রজেক্ট লিড) |
| `POST` | `/api/users/:id/invite` · `/collab` | টিমমেট ইনভাইট / কোলাবরেশন রিকোয়েস্ট |
| `PUT` | `/api/me/profile` | নাম, বায়ো, পোর্টফোলিও, স্কিল ইত্যাদি |
| `PUT` | `/api/me/settings` | অ্যাকাউন্ট, প্রাইভেসি, কোলাবরেশন প্রেফারেন্স |
| `POST` | `/api/me/skills/:name/endorse` | স্কিল এন্ডোর্সমেন্ট |
| `POST` | `/api/notifications/read` | সব নোটিফিকেশন read |

Collections: `users, projects, tasks, joinrequests, invitations, activities, notifications, reviews`.

---

## 🔄 Client Fallback (অফলাইন সাপোর্ট)
TeamUp Java ক্লায়েন্ট (`AuthStore.java`) স্বয়ংক্রিয়ভাবে `http://localhost:5000/api` কল করে। যদি কোনো কারণে ব্যাকএন্ড সার্ভার সাময়িকভাবে বন্ধ থাকে, ক্লায়েন্ট অ্যাপ ক্র্যাশ না করে স্বয়ংক্রিয়ভাবে লোকাল স্টোরেজে (`data/users.csv`) PBKDF2 হ্যাশিং সহ কাজ চালিয়ে যাবে।

লগইন করার পর Java ড্যাশবোর্ড (`DashboardSync.java`) `/api/dashboard` থেকে ইউজারের আসল ডাটা লোড করে এবং প্রতিটি অ্যাকশন (বুকমার্ক, জয়েন, টাস্ক, সেটিংস...) ব্যাকএন্ডে সেভ করে। লগইন ছাড়া (F2) বা ব্যাকএন্ড বন্ধ থাকলে ড্যাশবোর্ড আগের মতো বিল্ট-ইন ডেমো ডাটা দেখায়।
