const mongoose = require('mongoose');

const platformSettingSchema = new mongoose.Schema(
  {
    key: { type: String, unique: true, default: 'global' },
    platformName: { type: String, default: 'TeamUp' },
    contactEmail: { type: String, default: 'contact@teamup.edu' },
    supportEmail: { type: String, default: 'support@teamup.edu' },
    platformLogoText: { type: String, default: 'TU' },
    defaultLanguage: { type: String, default: 'English (US)' },
    adminPassword: { type: String, default: 'admin123' },
    notificationsEnabled: { type: Boolean, default: true },

    allowedDomains: {
      type: String,
      default: '@university.edu, @stanford.edu, @mit.edu, @berkeley.edu, @du.ac.bd, @buet.ac.bd',
    },
    universityDomainsList: {
      type: [String],
      default: ['@university.edu', '@stanford.edu', '@mit.edu', '@berkeley.edu', '@du.ac.bd', '@buet.ac.bd'],
    },
    strictDomainEnforcement: { type: Boolean, default: true },

    userRegistrationEnabled: { type: Boolean, default: true },
    requireEmailVerification: { type: Boolean, default: true },
    requireStudentId: { type: Boolean, default: true },
    defaultProfileVisibility: { type: String, default: 'Campus Only' },

    requireProjectApproval: { type: Boolean, default: false },
    projectCreationRole: { type: String, default: 'All Students' },
    maxProjectsPerStudent: { type: Number, default: 3 },
    autoArchiveInactiveProjects: { type: Boolean, default: true },

    minTeamSize: { type: Number, default: 2 },
    maxTeamSize: { type: Number, default: 6 },
    allowCrossDepartmentTeams: { type: Boolean, default: true },
    autoLockFullTeams: { type: Boolean, default: true },

    maxApplicationsPerStudent: { type: Number, default: 5 },
    allowApplicationWithdrawal: { type: Boolean, default: true },
    applicationExpiryDays: { type: Number, default: 14 },
    requireResumeOrPortfolio: { type: Boolean, default: false },

    spamSensitivityThreshold: { type: Number, default: 3 },
    maxLoginAttempts: { type: Number, default: 5 },
    sessionTimeoutMinutes: { type: Number, default: 30 },
    twoFactorAuthEnforced: { type: Boolean, default: false },

    emailNotificationsEnabled: { type: Boolean, default: true },
    inAppNotificationsEnabled: { type: Boolean, default: true },
    digestFrequency: { type: String, default: 'Instant' },
    announcementBroadcasts: { type: Boolean, default: true },

    maintenanceModeActive: { type: Boolean, default: false },
    maintenanceNotice: {
      type: String,
      default: 'TeamUp is undergoing scheduled maintenance upgrades. We will be back online shortly.',
    },

    appearanceTheme: { type: String, default: 'Light' },
    accentColorIndex: { type: Number, default: 0 },
    compactMode: { type: Boolean, default: false },

    lastBackupDate: { type: String, default: 'Nov 11, 2026, 04:30 AM' },
    lastBackupSize: { type: String, default: '42.8 MB' },
    dataRetentionDays: { type: Number, default: 365 },
  },
  { timestamps: true }
);

module.exports = mongoose.model('PlatformSetting', platformSettingSchema);

