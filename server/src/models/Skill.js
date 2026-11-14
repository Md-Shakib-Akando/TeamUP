const mongoose = require('mongoose');

const skillSchema = new mongoose.Schema(
  {
    skillId: { type: String, index: true }, // e.g. "S-1"
    name: { type: String, required: true, unique: true, trim: true },
    memberCount: { type: Number, default: 0 },
    active: { type: Boolean, default: true },
  },
  { timestamps: true }
);

module.exports = mongoose.model('SkillTag', skillSchema);

