const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const skillSchema = new mongoose.Schema(
  {
    name: { type: String, required: true, trim: true },
    category: { type: String, default: 'Tools', trim: true },
    endorsedBy: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],
    // Endorsements received before TeamUp tracked individual endorsers (seed data)
    baseEndorsements: { type: Number, default: 0 },
  },
  { _id: false }
);

const settingsSchema = new mongoose.Schema(
  {
    visibility: { type: String, enum: ['PUBLIC', 'TEAM_MEMBERS', 'PRIVATE'], default: 'PUBLIC' },
    showSkills: { type: Boolean, default: true },
    showActivity: { type: Boolean, default: false },
    categories: { type: [String], default: ['Web Development'] },
    roleFrontendDev: { type: Boolean, default: false },
    roleUiDev: { type: Boolean, default: false },
    roleBackendDev: { type: Boolean, default: false },
    roleProjectLead: { type: Boolean, default: false },
    weeklyAvailability: { type: String, default: '10–15 hours per week' },
    preferredCommitment: { type: String, default: '1–3 months' },
    collabMode: { type: String, enum: ['REMOTE', 'IN_PERSON', 'EITHER'], default: 'REMOTE' },
  },
  { _id: false }
);

const userSchema = new mongoose.Schema(
  {
    name: {
      type: String,
      required: [true, 'Please provide a name'],
      trim: true,
      maxlength: [80, 'Name cannot exceed 80 characters'],
    },
    email: {
      type: String,
      required: [true, 'Please provide an email'],
      unique: true,
      lowercase: true,
      trim: true,
      match: [
        /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/,
        'Please provide a valid email address',
      ],
    },
    password: {
      type: String,
      required: [true, 'Please provide a password'],
      minlength: [6, 'Password must be at least 6 characters'],
      select: true,
    },
    // Admin role is assigned manually directly in the MongoDB Atlas database.
    // Public registration defaults to 'Student' (or student's chosen discipline).
    role: {
      type: String,
      enum: ['Student', 'Developer', 'UI/UX Designer', 'Team Leader', 'Researcher', 'Freelancer', 'Member', 'Moderator', 'Admin'],
      default: 'Student',
    },
    status: {
      type: String,
      enum: ['Active', 'Suspended'],
      default: 'Active',
    },

    // ---- Public profile (shown on Find Teammates / My Profile) ----
    username: { type: String, trim: true, lowercase: true, sparse: true, unique: true },
    headline: { type: String, trim: true, default: '' }, // e.g. "UX/UI designer"
    university: { type: String, trim: true, default: '' },
    department: { type: String, trim: true, default: '' },
    gradYear: { type: String, trim: true, default: '' },
    location: { type: String, trim: true, default: '' },
    bio: { type: String, trim: true, default: '', maxlength: 600 },
    github: { type: String, trim: true, default: '' },
    portfolio: { type: String, trim: true, default: '' },
    availability: { type: String, trim: true, default: 'Available · 5-10 hrs / week' },
    experience: { type: String, enum: ['Beginner', 'Intermediate', 'Advanced'], default: 'Beginner' },
    skills: { type: [skillSchema], default: [] },
    avatarBg: { type: String, default: '#EDE9FE' },
    avatarFg: { type: String, default: '#6D28D9' },
    // Seed-only hints used to keep demo match scores / ratings stable
    matchHint: { type: Number, default: 0 },
    matchDesc: { type: String, default: '' },
    ratingBase: { type: Number, min: 0, max: 5, default: 0 },
    reviewsBase: { type: Number, min: 0, default: 0 },

    bookmarks: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Project' }],
    settings: { type: settingsSchema, default: () => ({}) },
    passwordChangedAt: { type: Date },
    isDemo: { type: Boolean, default: false },
  },
  {
    timestamps: true,
  }
);

// Hash password before saving if it has been modified
userSchema.pre('save', async function (next) {
  if (!this.isModified('password')) {
    return next();
  }
  const salt = await bcrypt.genSalt(10);
  this.password = await bcrypt.hash(this.password, salt);
  this.passwordChangedAt = new Date();
  next();
});

// Compare password
userSchema.methods.matchPassword = async function (enteredPassword) {
  if (!this.password) return false;
  return await bcrypt.compare(enteredPassword, this.password);
};

// Safe JSON serialization
userSchema.methods.toSafeObject = function () {
  return {
    id: this._id,
    name: this.name,
    email: this.email,
    role: this.role,
    createdAt: this.createdAt,
  };
};

module.exports = mongoose.model('User', userSchema);
