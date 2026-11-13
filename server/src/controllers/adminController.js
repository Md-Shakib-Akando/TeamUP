const User = require('../models/User');
const Project = require('../models/Project');
const Report = require('../models/Report');
const Category = require('../models/Category');
const SkillTag = require('../models/Skill');
const Review = require('../models/Review');
const AuditLog = require('../models/AuditLog');
const PlatformSetting = require('../models/PlatformSetting');
const bcrypt = require('bcryptjs');

// Format date helper: "Nov 14, 2026"
function formatDate(d) {
  if (!d) return 'Nov 14, 2026';
  const date = new Date(d);
  if (isNaN(date.getTime())) return String(d);
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const month = months[date.getMonth()];
  const day = String(date.getDate()).padStart(2, '0');
  const year = date.getFullYear();
  return `${month} ${day}, ${year}`;
}

// Compute initials
function computeInitials(name) {
  if (!name) return 'TU';
  const parts = name.trim().split(/\s+/);
  if (parts.length >= 2) {
    return (parts[0][0] + parts[1][0]).toUpperCase();
  }
  return name.trim().slice(0, 2).toUpperCase();
}

// Helper to log audit activity into MongoDB
async function logAudit(actorName, actorInitials, action, target, success = true) {
  try {
    const count = await AuditLog.countDocuments();
    const now = new Date();
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const timeStr = now.toTimeString().split(' ')[0];
    const line1 = `${months[now.getMonth()]} ${now.getDate()} · ${timeStr}`;
    const line2 = `${now.getFullYear()} · UTC`;

    await AuditLog.create({
      logId: `L-${count + 1}`,
      actorName: actorName || 'Jordan Lee',
      actorInitials: actorInitials || 'JL',
      action,
      target,
      timestampLine1: line1,
      timestampLine2: line2,
      success,
    });
  } catch (err) {
    console.error('[Admin AuditLog] Error saving log:', err.message);
  }
}

// Seed initial admin data if collections are empty
async function ensureSeedData() {
  try {
    // 1. Ensure Platform Settings
    let settings = await PlatformSetting.findOne({ key: 'global' });
    if (!settings) {
      settings = await PlatformSetting.create({ key: 'global' });
    }

    // 2. Ensure Categories
    const catCount = await Category.countDocuments();
    if (catCount === 0) {
      await Category.insertMany([
        { categoryId: 'C-1', name: 'Web Development', projectCount: 124, active: true },
        { categoryId: 'C-2', name: 'AI & Machine Learning', projectCount: 86, active: true },
        { categoryId: 'C-3', name: 'Design', projectCount: 78, active: true },
        { categoryId: 'C-4', name: 'Research', projectCount: 63, active: true },
        { categoryId: 'C-5', name: 'Sustainability', projectCount: 47, active: true },
        { categoryId: 'C-6', name: 'Other / Legacy', projectCount: 0, active: false },
      ]);
    }

    // 3. Ensure Skills
    const skillCount = await SkillTag.countDocuments();
    if (skillCount === 0) {
      await SkillTag.insertMany([
        { skillId: 'S-1', name: 'React', memberCount: 1248, active: true },
        { skillId: 'S-2', name: 'TypeScript', memberCount: 986, active: true },
        { skillId: 'S-3', name: 'Node.js', memberCount: 842, active: true },
        { skillId: 'S-4', name: 'UI Design', memberCount: 728, active: true },
        { skillId: 'S-5', name: 'User Research', memberCount: 412, active: true },
        { skillId: 'S-6', name: 'AngularJS', memberCount: 23, active: false },
      ]);
    }

    // 4. Ensure Reports
    const reportCount = await Report.countDocuments();
    if (reportCount === 0) {
      await Report.insertMany([
        {
          reportId: 'R-1048',
          type: 'Message',
          title: 'StudyBuddy AI conversation',
          category: 'Harassment',
          severity: 'High',
          reporter: 'Priya Shah',
          reporterInitials: 'PS',
          date: 'Oct 16, 2026',
          timeUtc: '9:12 AM UTC',
          status: 'Pending',
          reporterDesc: 'Evan has repeatedly sent hostile messages after I declined his project invitation. I asked him to stop, but the messages continued this morning.',
          flaggedAuthor: 'Evan Reed · StudyBuddy AI',
          flaggedContent: '"You\'re wasting everyone\'s time. Don\'t bother applying to any of our teams again."',
          flaggedTime: 'Oct 16, 2026 · 8:58 AM UTC',
        },
        {
          reportId: 'R-1047',
          type: 'Project',
          title: 'SkillSwap Marketplace',
          category: 'Misleading content',
          severity: 'High',
          reporter: 'Maya Patel',
          reporterInitials: 'MP',
          date: 'Oct 15, 2026',
          timeUtc: '3:24 PM UTC',
          status: 'In review',
          reporterDesc: 'Project promises paid internships without required university authorization or credential verification.',
          flaggedAuthor: 'Noah Williams · SkillSwap Marketplace',
          flaggedContent: '"Guaranteed $45/hr remote research internship upon joining."',
          flaggedTime: 'Oct 15, 2026 · 2:10 PM UTC',
        },
        {
          reportId: 'R-1046',
          type: 'Message',
          title: 'Unsolicited project invite',
          category: 'Spam',
          severity: 'Medium',
          reporter: 'Alex Morgan',
          reporterInitials: 'AM',
          date: 'Oct 15, 2026',
          timeUtc: '11:05 AM UTC',
          status: 'Pending',
          reporterDesc: 'Mass bulk invitations sent to all freshmen engineering students without relevance to their majors.',
          flaggedAuthor: 'Evan Reed · StudyBuddy AI',
          flaggedContent: '"Join StudyBuddy now and receive automated exam answers."',
          flaggedTime: 'Oct 15, 2026 · 10:45 AM UTC',
        },
        {
          reportId: 'R-1045',
          type: 'Profile',
          title: 'User profile biography',
          category: 'Inappropriate content',
          severity: 'Low',
          reporter: 'Sam Chen',
          reporterInitials: 'SC',
          date: 'Oct 14, 2026',
          timeUtc: '6:18 PM UTC',
          status: 'Pending',
          reporterDesc: 'Profile bio contains offensive external URL redirects.',
          flaggedAuthor: 'Anonymous User',
          flaggedContent: '"Check out external proxy site for answers."',
          flaggedTime: 'Oct 14, 2026 · 6:00 PM UTC',
        },
        {
          reportId: 'R-1044',
          type: 'Comment',
          title: 'Research Commons comment',
          category: 'Off-topic content',
          severity: 'Low',
          reporter: 'Sofia Garcia',
          reporterInitials: 'SG',
          date: 'Oct 14, 2026',
          timeUtc: '1:15 PM UTC',
          status: 'Resolved',
          reporterDesc: 'Comment advertising commercial crypto tutoring service in research discussion.',
          flaggedAuthor: 'Sam Chen · Research Commons',
          flaggedContent: '"Resolved by moderator - non-violating educational reference."',
          flaggedTime: 'Oct 14, 2026 · 1:00 PM UTC',
        },
      ]);
    }

    // 5. Ensure Reviews
    const revCount = await Review.countDocuments();
    if (revCount === 0) {
      await Review.insertMany([
        {
          reviewId: 'REV-501',
          reviewerName: 'Alex Morgan',
          reviewerInitials: 'AM',
          revieweeName: 'Priya Shah',
          projectName: 'Open Design Library',
          roleOrContext: 'Lead Design System',
          rating: 5,
          reviewText: 'Incredible attention to accessibility standards! Created 30+ reusable Figma components and led weekly design reviews with outstanding clarity.',
          reviewType: 'Team Member Review',
          date: 'Nov 08, 2026',
          status: 'Published',
          helpfulVotes: 18,
          verifiedCollaboration: true,
        },
        {
          reviewId: 'REV-502',
          reviewerName: 'Sam Chen',
          reviewerInitials: 'SC',
          revieweeName: 'Sofia Garcia',
          projectName: 'Research Commons',
          roleOrContext: 'Data Analyst',
          rating: 5,
          reviewText: 'Exceptional research methodology and clean documentation. Delivered data pipelines ahead of schedule with zero errors.',
          reviewType: 'User Rating',
          date: 'Nov 06, 2026',
          status: 'Published',
          helpfulVotes: 9,
          verifiedCollaboration: true,
        },
        {
          reviewId: 'REV-503',
          reviewerName: 'Noah Williams',
          reviewerInitials: 'NW',
          revieweeName: 'Sam Chen',
          projectName: 'SkillSwap Marketplace',
          roleOrContext: 'Backend Contributor',
          rating: 1,
          reviewText: 'Useless contributor, terrible code quality and should be kicked out of the university program immediately.',
          reviewType: 'Team Member Review',
          date: 'Nov 04, 2026',
          status: 'Reported',
          reportReason: 'Hostile language, personal attack & harassment',
          reportedBy: 'Sam Chen',
          helpfulVotes: 0,
          verifiedCollaboration: false,
        },
        {
          reviewId: 'REV-504',
          reviewerName: 'Maya Patel',
          reviewerInitials: 'MP',
          revieweeName: 'Alex Morgan',
          projectName: 'Campus Carbon Tracker',
          roleOrContext: 'Frontend Engineer',
          rating: 4,
          reviewText: 'Great collaboration on the carbon analytics dashboard. Very responsive on Slack and receptive to UI feedback.',
          reviewType: 'User Rating',
          date: 'Nov 02, 2026',
          status: 'Published',
          helpfulVotes: 12,
          verifiedCollaboration: true,
        },
        {
          reviewId: 'REV-505',
          reviewerName: 'Evan Reed',
          reviewerInitials: 'ER',
          revieweeName: 'Maya Patel',
          projectName: 'Accessible Campus Map',
          roleOrContext: 'Tester',
          rating: 1,
          reviewText: "Don't work with this person. Check out bit.ly/cheat-sheets-2026 for actual project answers and guaranteed grades!",
          reviewType: 'User Rating',
          date: 'Oct 29, 2026',
          status: 'Reported',
          reportReason: 'Spam advertising & suspicious malicious URL link',
          reportedBy: 'Maya Patel',
          helpfulVotes: 1,
          verifiedCollaboration: false,
        },
        {
          reviewId: 'REV-506',
          reviewerName: 'Sofia Garcia',
          reviewerInitials: 'SG',
          revieweeName: 'Jordan Lee',
          projectName: 'Community Data Atlas',
          roleOrContext: 'Mentor',
          rating: 5,
          reviewText: "Jordan's guidance on SQL optimization and spatial queries improved our map rendering latency by over 400%. Outstanding leadership!",
          reviewType: 'Team Member Review',
          date: 'Oct 27, 2026',
          status: 'Published',
          helpfulVotes: 24,
          verifiedCollaboration: true,
        },
        {
          reviewId: 'REV-507',
          reviewerName: 'Priya Shah',
          reviewerInitials: 'PS',
          revieweeName: 'Evan Reed',
          projectName: 'StudyBuddy AI',
          roleOrContext: 'API Developer',
          rating: 4,
          reviewText: 'Solid backend implementation on the socket server and prompt responses. Would love slightly earlier pull requests before sprint cutoffs.',
          reviewType: 'Team Member Review',
          date: 'Oct 24, 2026',
          status: 'Published',
          helpfulVotes: 7,
          verifiedCollaboration: true,
        },
        {
          reviewId: 'REV-508',
          reviewerName: 'Anonymous Student',
          reviewerInitials: 'AS',
          revieweeName: 'Alex Morgan',
          projectName: 'Local Food Network',
          roleOrContext: 'Scrum Master',
          rating: 2,
          reviewText: 'Completely disorganized sprint planning. Demanded excessive weekend work without proper scope negotiation.',
          reviewType: 'Peer Collaboration',
          date: 'Oct 20, 2026',
          status: 'Under review',
          reportReason: 'Disputed performance claim by team leads',
          reportedBy: 'Alex Morgan',
          helpfulVotes: 3,
          verifiedCollaboration: true,
        },
        {
          reviewId: 'REV-509',
          reviewerName: 'Maya Patel',
          reviewerInitials: 'MP',
          revieweeName: 'Priya Shah',
          projectName: 'Open Design Library',
          roleOrContext: 'UX Researcher',
          rating: 5,
          reviewText: 'Peerless user empathy and structured interview synthesis. Working together on the usability sprint was a highlight of this semester.',
          reviewType: 'User Rating',
          date: 'Oct 15, 2026',
          status: 'Published',
          helpfulVotes: 15,
          verifiedCollaboration: true,
        },
      ]);
    }

    // 6. Ensure Audit Logs
    const logCount = await AuditLog.countDocuments();
    if (logCount === 0) {
      await AuditLog.insertMany([
        {
          logId: 'L-1',
          actorName: 'Jordan Lee',
          actorInitials: 'JL',
          action: 'Suspended user',
          target: 'Noah Williams',
          timestampLine1: 'Oct 16 · 09:52:14',
          timestampLine2: '2026 · UTC',
          success: true,
        },
        {
          logId: 'L-2',
          actorName: 'Sam Chen',
          actorInitials: 'SC',
          action: 'Resolved report',
          target: 'R-1044 · Research Commons',
          timestampLine1: 'Oct 16 · 08:30:42',
          timestampLine2: '2026 · UTC',
          success: true,
        },
        {
          logId: 'L-3',
          actorName: 'Jordan Lee',
          actorInitials: 'JL',
          action: 'Updated moderation rule',
          target: 'Auto-flag repeated spam',
          timestampLine1: 'Oct 15 · 16:24:08',
          timestampLine2: '2026 · UTC',
          success: true,
        },
        {
          logId: 'L-4',
          actorName: 'Jordan Lee',
          actorInitials: 'JL',
          action: 'Changed user role',
          target: 'Sam Chen → Moderator',
          timestampLine1: 'Oct 15 · 14:12:33',
          timestampLine2: '2026 · UTC',
          success: true,
        },
        {
          logId: 'L-5',
          actorName: 'Jordan Lee',
          actorInitials: 'JL',
          action: 'Exported user list',
          target: 'Users · October 2026',
          timestampLine1: 'Oct 15 · 11:06:19',
          timestampLine2: '2026 · UTC',
          success: true,
        },
        {
          logId: 'L-6',
          actorName: 'Sam Chen',
          actorInitials: 'SC',
          action: 'Removed content',
          target: 'R-1039 · Project comment',
          timestampLine1: 'Oct 14 · 17:45:01',
          timestampLine2: '2026 · UTC',
          success: true,
        },
        {
          logId: 'L-7',
          actorName: 'Jordan Lee',
          actorInitials: 'JL',
          action: 'Disabled skill tag',
          target: 'AngularJS',
          timestampLine1: 'Oct 14 · 13:08:56',
          timestampLine2: '2026 · UTC',
          success: true,
        },
        {
          logId: 'L-8',
          actorName: 'Sam Chen',
          actorInitials: 'SC',
          action: 'Changed user role',
          target: 'Jordan Lee → Member',
          timestampLine1: 'Oct 14 · 10:18:22',
          timestampLine2: '2026 · UTC',
          success: false,
        },
        {
          logId: 'L-9',
          actorName: 'Jordan Lee',
          actorInitials: 'JL',
          action: 'Created category',
          target: 'Sustainability',
          timestampLine1: 'Oct 13 · 15:32:41',
          timestampLine2: '2026 · UTC',
          success: true,
        },
      ]);
    }

    // 7. Ensure Default Users exist (Admin Jordan Lee & seeded users)
    const userCount = await User.countDocuments();
    if (userCount === 0) {
      const demoPass = await bcrypt.hash('teamup123', 10);
      const adminPass = await bcrypt.hash('admin123', 10);
      await User.insertMany([
        { name: 'Jordan Lee', email: 'jordan.lee@teamup.com', password: adminPass, role: 'Admin', status: 'Active' },
        { name: 'Alex Morgan', email: 'alex.morgan@example.com', password: demoPass, role: 'Member', status: 'Active' },
        { name: 'Priya Shah', email: 'priyashah@example.com', password: demoPass, role: 'Member', status: 'Active' },
        { name: 'Sam Chen', email: 'sam.chen@example.com', password: demoPass, role: 'Moderator', status: 'Active' },
        { name: 'Noah Williams', email: 'noah.w@example.com', password: demoPass, role: 'Member', status: 'Suspended' },
        { name: 'Maya Patel', email: 'maya.patel@example.com', password: demoPass, role: 'Member', status: 'Active' },
        { name: 'Evan Reed', email: 'evan.reed@example.com', password: demoPass, role: 'Member', status: 'Active' },
        { name: 'Sofia Garcia', email: 'sofia.g@example.com', password: demoPass, role: 'Member', status: 'Active' },
      ]);
    } else {
      // Ensure admin exists
      const admin = await User.findOne({ email: 'admin@teamup.edu' });
      if (!admin) {
        const adminPass = await bcrypt.hash('admin123', 10);
        await User.create({
          name: 'Jordan Lee',
          email: 'admin@teamup.edu',
          password: adminPass,
          role: 'Admin',
          status: 'Active',
        });
      }
    }

    // 8. Ensure Initial Projects exist
    const projectCount = await Project.countDocuments();
    if (projectCount === 0) {
      await Project.insertMany([
        {
          title: 'Campus Carbon Tracker',
          category: 'Sustainability',
          ownerName: 'Alex Morgan',
          teamSize: '5 / 7',
          status: 'Active',
          description: 'Campus-wide carbon footprint estimation and solar dashboard.',
        },
        {
          title: 'Open Design Library',
          category: 'Design',
          ownerName: 'Priya Shah',
          teamSize: '6 / 6',
          status: 'Completed',
          description: 'Open source design token system for university research labs.',
        },
        {
          title: 'Research Commons',
          category: 'Research',
          ownerName: 'Sam Chen',
          teamSize: '4 / 6',
          status: 'Active',
          description: 'Cross-faculty academic paper collaboration hub.',
        },
        {
          title: 'SkillSwap Marketplace',
          category: 'Web Development',
          ownerName: 'Noah Williams',
          teamSize: '3 / 5',
          status: 'Under review',
          description: 'Student peer mentoring and skill-sharing platform.',
        },
        {
          title: 'Accessible Campus Map',
          category: 'Design',
          ownerName: 'Maya Patel',
          teamSize: '4 / 5',
          status: 'Recruiting',
          description: 'Interactive navigation maps optimized for students with disabilities.',
        },
        {
          title: 'StudyBuddy AI',
          category: 'AI & Machine Learning',
          ownerName: 'Evan Reed',
          teamSize: '5 / 6',
          status: 'Active',
          description: 'Personalized study scheduling and AI quiz generator.',
        },
        {
          title: 'Community Data Atlas',
          category: 'Research',
          ownerName: 'Sofia Garcia',
          teamSize: '3 / 4',
          status: 'Active',
          description: 'Open regional environmental data visualization for student researchers.',
        },
        {
          title: 'Local Food Network',
          category: 'Sustainability',
          ownerName: 'Maya Patel',
          teamSize: '4 / 6',
          status: 'Recruiting',
          description: 'Connecting local student dining halls with organic farm cooperatives.',
        },
      ]);
    }
  } catch (err) {
    console.error('[Admin Seed] Error ensuring seed data:', err.message);
  }
}

// =========================================================================
// GET /api/admin/bootstrap
// Returns full admin data in one fast round-trip
// =========================================================================
const getAdminBootstrap = async (req, res) => {
  try {
    await ensureSeedData();

    // 1. Fetch Users
    const rawUsers = await User.find({}).sort({ createdAt: -1 });
    let uIdx = 100;
    const users = rawUsers.map((u) => {
      uIdx++;
      return {
        id: `U-${uIdx}`,
        mongoId: String(u._id),
        name: u.name,
        initials: computeInitials(u.name),
        email: u.email,
        role: u.role || 'Member',
        joined: formatDate(u.createdAt),
        status: u.status || 'Active',
      };
    });

    // 2. Fetch Projects
    const rawProjects = await Project.find({}).sort({ createdAt: -1 });
    let pIdx = 200;
    const projects = rawProjects.map((p) => {
      pIdx++;
      const currentMembers = (p.members && p.members.length) || 1;
      const maxM = p.maxMembers || 6;
      const teamSizeStr = p.teamSize || `${currentMembers} / ${maxM}`;
      return {
        id: `P-${pIdx}`,
        mongoId: String(p._id),
        name: p.title || p.name || 'Untitled Project',
        category: p.category || 'Web Development',
        owner: p.ownerName || (p.owner && p.owner.name) || 'Jordan Lee',
        teamSize: teamSizeStr,
        status: p.status || 'Active',
        created: formatDate(p.createdAt),
        description: p.description || '',
      };
    });

    // 3. Fetch Reports
    const rawReports = await Report.find({}).sort({ createdAt: -1 });
    const reports = rawReports.map((r, i) => ({
      id: r.reportId || `R-${1048 - i}`,
      mongoId: String(r._id),
      type: r.type || 'Message',
      title: r.title,
      category: r.category,
      severity: r.severity,
      reporter: r.reporter,
      reporterInitials: r.reporterInitials || computeInitials(r.reporter),
      date: r.date || formatDate(r.createdAt),
      timeUtc: r.timeUtc || '12:00 PM UTC',
      status: r.status,
      reporterDesc: r.reporterDesc,
      flaggedAuthor: r.flaggedAuthor,
      flaggedContent: r.flaggedContent,
      flaggedTime: r.flaggedTime,
      internalNote: r.internalNote || '',
    }));

    // 4. Fetch Categories & Skills
    const rawCategories = await Category.find({}).sort({ createdAt: 1 });
    const categories = rawCategories.map((c, i) => ({
      id: c.categoryId || `C-${i + 1}`,
      mongoId: String(c._id),
      name: c.name,
      projectCount: c.projectCount || 0,
      active: c.active !== false,
    }));

    const rawSkills = await SkillTag.find({}).sort({ createdAt: 1 });
    const skills = rawSkills.map((s, i) => ({
      id: s.skillId || `S-${i + 1}`,
      mongoId: String(s._id),
      name: s.name,
      memberCount: s.memberCount || 0,
      active: s.active !== false,
    }));

    // 5. Fetch Reviews
    const rawReviews = await Review.find({}).sort({ createdAt: -1 });
    const reviews = rawReviews.map((rev, i) => ({
      id: rev.reviewId || `REV-${501 + i}`,
      mongoId: String(rev._id),
      reviewerName: rev.reviewerName || 'Anonymous',
      reviewerInitials: rev.reviewerInitials || computeInitials(rev.reviewerName),
      revieweeName: rev.revieweeName || 'Student',
      projectName: rev.projectName || 'Community Project',
      roleOrContext: rev.roleOrContext || 'Contributor',
      rating: rev.rating || 5,
      reviewText: rev.reviewText || rev.feedback || '',
      reviewType: rev.reviewType || 'Team Member Review',
      date: rev.date || formatDate(rev.createdAt),
      status: rev.status || 'Published',
      reportReason: rev.reportReason || null,
      reportedBy: rev.reportedBy || null,
      helpfulVotes: rev.helpfulVotes || 0,
      verifiedCollaboration: rev.verifiedCollaboration !== false,
    }));

    // 6. Fetch Audit Logs
    const rawLogs = await AuditLog.find({}).sort({ createdAt: -1 }).limit(50);
    const auditLogs = rawLogs.map((l, i) => ({
      id: l.logId || `L-${i + 1}`,
      mongoId: String(l._id),
      actorName: l.actorName || 'Jordan Lee',
      actorInitials: l.actorInitials || computeInitials(l.actorName),
      action: l.action,
      target: l.target,
      timestampLine1: l.timestampLine1 || formatDate(l.createdAt),
      timestampLine2: l.timestampLine2 || 'UTC',
      success: l.success !== false,
    }));

    // 7. Fetch Settings
    let settingsDoc = await PlatformSetting.findOne({ key: 'global' });
    if (!settingsDoc) {
      settingsDoc = await PlatformSetting.create({ key: 'global' });
    }

    const settings = {
      platformName: settingsDoc.platformName,
      contactEmail: settingsDoc.contactEmail,
      supportEmail: settingsDoc.supportEmail,
      platformLogoText: settingsDoc.platformLogoText,
      defaultLanguage: settingsDoc.defaultLanguage,
      adminPassword: settingsDoc.adminPassword,
      notificationsEnabled: settingsDoc.notificationsEnabled,
      allowedDomains: settingsDoc.allowedDomains,
      universityDomainsList: settingsDoc.universityDomainsList,
      strictDomainEnforcement: settingsDoc.strictDomainEnforcement,
      userRegistrationEnabled: settingsDoc.userRegistrationEnabled,
      requireEmailVerification: settingsDoc.requireEmailVerification,
      requireStudentId: settingsDoc.requireStudentId,
      defaultProfileVisibility: settingsDoc.defaultProfileVisibility,
      requireProjectApproval: settingsDoc.requireProjectApproval,
      projectCreationRole: settingsDoc.projectCreationRole,
      maxProjectsPerStudent: settingsDoc.maxProjectsPerStudent,
      autoArchiveInactiveProjects: settingsDoc.autoArchiveInactiveProjects,
      minTeamSize: settingsDoc.minTeamSize,
      maxTeamSize: settingsDoc.maxTeamSize,
      allowCrossDepartmentTeams: settingsDoc.allowCrossDepartmentTeams,
      autoLockFullTeams: settingsDoc.autoLockFullTeams,
      maxApplicationsPerStudent: settingsDoc.maxApplicationsPerStudent,
      allowApplicationWithdrawal: settingsDoc.allowApplicationWithdrawal,
      applicationExpiryDays: settingsDoc.applicationExpiryDays,
      requireResumeOrPortfolio: settingsDoc.requireResumeOrPortfolio,
      spamSensitivityThreshold: settingsDoc.spamSensitivityThreshold,
      maxLoginAttempts: settingsDoc.maxLoginAttempts,
      sessionTimeoutMinutes: settingsDoc.sessionTimeoutMinutes,
      twoFactorAuthEnforced: settingsDoc.twoFactorAuthEnforced,
      emailNotificationsEnabled: settingsDoc.emailNotificationsEnabled,
      inAppNotificationsEnabled: settingsDoc.inAppNotificationsEnabled,
      digestFrequency: settingsDoc.digestFrequency,
      announcementBroadcasts: settingsDoc.announcementBroadcasts,
      maintenanceModeActive: settingsDoc.maintenanceModeActive,
      maintenanceNotice: settingsDoc.maintenanceNotice,
      appearanceTheme: settingsDoc.appearanceTheme,
      accentColorIndex: settingsDoc.accentColorIndex,
      compactMode: settingsDoc.compactMode,
      lastBackupDate: settingsDoc.lastBackupDate,
      lastBackupSize: settingsDoc.lastBackupSize,
      dataRetentionDays: settingsDoc.dataRetentionDays,
    };

    // Calculate Platform Statistics
    const totalUsers = Math.max(12480, 12480 + (users.length - 8));
    const totalProjects = Math.max(482, 482 + (projects.length - 8));
    const pendingReportsCount = reports.filter((r) => r.status === 'Pending' || r.status === 'In review').length;
    const activeProjectsCount = projects.filter((p) => p.status === 'Active').length;
    const activeTeamsCount = Math.max(120, 186 + (activeProjectsCount - 4));

    return res.status(200).json({
      success: true,
      message: 'Admin dashboard data loaded from MongoDB Atlas',
      data: {
        users,
        projects,
        reports,
        categories,
        skills,
        reviews,
        auditLogs,
        settings,
        stats: {
          totalUsers,
          totalProjects,
          pendingReportsCount,
          activeTeamsCount,
          projectSuccessRate: 94.6,
          averageTeamSize: 4.6,
          collaborationRequests: 1642,
        },
      },
    });
  } catch (error) {
    console.error('[Admin Bootstrap Error]:', error);
    return res.status(500).json({
      success: false,
      message: 'Failed to load admin data: ' + error.message,
    });
  }
};

// =========================================================================
// USERS CRUD
// =========================================================================

const createUser = async (req, res) => {
  try {
    const { name, email, role, password } = req.body;
    if (!name || !name.trim()) {
      return res.status(400).json({ success: false, message: 'Please provide user name' });
    }
    const cleanName = name.trim();
    const cleanEmail = (email && email.trim()) || `${cleanName.toLowerCase().replace(/\s+/g, '.')}@example.com`;
    const cleanRole = role || 'Member';

    const existing = await User.findOne({ email: cleanEmail.toLowerCase() });
    if (existing) {
      return res.status(400).json({ success: false, message: 'User with this email already exists' });
    }

    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(password || 'teamup123', salt);

    const newUser = await User.create({
      name: cleanName,
      email: cleanEmail.toLowerCase(),
      password: hashedPassword,
      role: cleanRole,
      status: 'Active',
    });

    await logAudit('Jordan Lee', 'JL', 'Added new user', `${cleanName} (${cleanRole})`, true);

    return res.status(201).json({
      success: true,
      message: 'User created successfully in database',
      user: {
        mongoId: String(newUser._id),
        name: newUser.name,
        initials: computeInitials(newUser.name),
        email: newUser.email,
        role: newUser.role,
        joined: formatDate(newUser.createdAt),
        status: newUser.status,
      },
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

const updateUserRole = async (req, res) => {
  try {
    const { id } = req.params;
    const { role, name } = req.body;
    if (!role) {
      return res.status(400).json({ success: false, message: 'Role is required' });
    }

    let user;
    if (id && id.match(/^[0-9a-fA-F]{24}$/)) {
      user = await User.findById(id);
    }
    if (!user && name) {
      user = await User.findOne({ name: new RegExp(`^${name.trim()}$`, 'i') });
    }
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found in database' });
    }

    const oldRole = user.role;
    user.role = role;
    await user.save();

    await logAudit('Jordan Lee', 'JL', 'Changed user role', `${user.name} → ${role}`, true);

    return res.status(200).json({
      success: true,
      message: `User role updated from ${oldRole} to ${role}`,
      user: {
        mongoId: String(user._id),
        name: user.name,
        role: user.role,
      },
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

const updateUserStatus = async (req, res) => {
  try {
    const { id } = req.params;
    const { status, name } = req.body; // 'Active' or 'Suspended'
    if (!status) {
      return res.status(400).json({ success: false, message: 'Status is required' });
    }

    let user;
    if (id && id.match(/^[0-9a-fA-F]{24}$/)) {
      user = await User.findById(id);
    }
    if (!user && name) {
      user = await User.findOne({ name: new RegExp(`^${name.trim()}$`, 'i') });
    }
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    user.status = status;
    await user.save();

    const actionText = status === 'Suspended' ? 'Suspended user' : 'Reactivated user';
    await logAudit('Jordan Lee', 'JL', actionText, user.name, true);

    return res.status(200).json({
      success: true,
      message: `User is now ${status}`,
      user: {
        mongoId: String(user._id),
        name: user.name,
        status: user.status,
      },
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

const deleteUser = async (req, res) => {
  try {
    const { id } = req.params;
    const { name } = req.query;

    let user;
    if (id && id.match(/^[0-9a-fA-F]{24}$/)) {
      user = await User.findById(id);
    }
    if (!user && name) {
      user = await User.findOne({ name: new RegExp(`^${name.trim()}$`, 'i') });
    }
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found to delete' });
    }

    const userName = user.name;
    await User.findByIdAndDelete(user._id);

    await logAudit('Jordan Lee', 'JL', 'Deleted user', userName, true);

    return res.status(200).json({
      success: true,
      message: `User ${userName} deleted successfully from database`,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

// =========================================================================
// PROJECTS CRUD
// =========================================================================

const createProject = async (req, res) => {
  try {
    const { name, title, category, owner, teamSize, status, description } = req.body;
    const projTitle = (title || name || '').trim();
    if (!projTitle) {
      return res.status(400).json({ success: false, message: 'Project name is required' });
    }

    const newProj = await Project.create({
      title: projTitle,
      category: category || 'Web Development',
      ownerName: owner || 'Jordan Lee',
      teamSize: teamSize || '1 / 6',
      status: status || 'Active',
      description: description || 'Community project initiative.',
    });

    await logAudit('Jordan Lee', 'JL', 'Created project', `${projTitle} (${newProj.category})`, true);

    return res.status(201).json({
      success: true,
      message: 'Project created in database',
      project: {
        mongoId: String(newProj._id),
        name: newProj.title,
        category: newProj.category,
        owner: newProj.ownerName,
        teamSize: newProj.teamSize,
        status: newProj.status,
        created: formatDate(newProj.createdAt),
        description: newProj.description,
      },
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

const updateProjectStatus = async (req, res) => {
  try {
    const { id } = req.params;
    const { status, name } = req.body;
    if (!status) {
      return res.status(400).json({ success: false, message: 'Status is required' });
    }

    let project;
    if (id && id.match(/^[0-9a-fA-F]{24}$/)) {
      project = await Project.findById(id);
    }
    if (!project && name) {
      project = await Project.findOne({ title: new RegExp(`^${name.trim()}$`, 'i') });
    }
    if (!project) {
      return res.status(404).json({ success: false, message: 'Project not found' });
    }

    const oldStatus = project.status;
    project.status = status;
    await project.save();

    await logAudit('Jordan Lee', 'JL', 'Updated project status', `${project.title} (${oldStatus} → ${status})`, true);

    return res.status(200).json({
      success: true,
      message: `Project status updated to ${status}`,
      project: {
        mongoId: String(project._id),
        name: project.title,
        status: project.status,
      },
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

const deleteProject = async (req, res) => {
  try {
    const { id } = req.params;
    const { name } = req.query;

    let project;
    if (id && id.match(/^[0-9a-fA-F]{24}$/)) {
      project = await Project.findById(id);
    }
    if (!project && name) {
      project = await Project.findOne({ title: new RegExp(`^${name.trim()}$`, 'i') });
    }
    if (!project) {
      return res.status(404).json({ success: false, message: 'Project not found to delete' });
    }

    const title = project.title;
    await Project.findByIdAndDelete(project._id);

    await logAudit('Jordan Lee', 'JL', 'Deleted project', title, true);

    return res.status(200).json({
      success: true,
      message: `Project ${title} deleted successfully from database`,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

// =========================================================================
// REPORTS & MODERATION
// =========================================================================

const resolveReport = async (req, res) => {
  try {
    const { id } = req.params;
    const { note } = req.body;

    let report;
    if (id && id.match(/^[0-9a-fA-F]{24}$/)) {
      report = await Report.findById(id);
    }
    if (!report) {
      report = await Report.findOne({ reportId: id });
    }
    if (!report) {
      return res.status(404).json({ success: false, message: 'Report not found' });
    }

    report.status = 'Resolved';
    if (note) report.internalNote = note;
    await report.save();

    await logAudit('Jordan Lee', 'JL', 'Resolved report', `${report.reportId || report._id} · ${report.title}`, true);

    return res.status(200).json({
      success: true,
      message: 'Report resolved in database',
      report,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

const actionReport = async (req, res) => {
  try {
    const { id } = req.params;
    const { action, note } = req.body; // 'warn', 'remove', 'escalate'

    let report;
    if (id && id.match(/^[0-9a-fA-F]{24}$/)) {
      report = await Report.findById(id);
    }
    if (!report) {
      report = await Report.findOne({ reportId: id });
    }
    if (!report) {
      return res.status(404).json({ success: false, message: 'Report not found' });
    }

    if (note) report.internalNote = note;

    if (action === 'warn') {
      report.status = 'In review';
      await logAudit('Jordan Lee', 'JL', 'Issued warning', `${report.flaggedAuthor} (Report ${report.reportId || id})`, true);
    } else if (action === 'remove') {
      report.status = 'Resolved';
      await logAudit('Jordan Lee', 'JL', 'Removed content', `${report.reportId || id} · ${report.type}`, true);
    } else if (action === 'escalate') {
      report.status = 'In review';
      await logAudit('Jordan Lee', 'JL', 'Escalated report', `${report.reportId || id} to safety council`, true);
    }
    await report.save();

    return res.status(200).json({
      success: true,
      message: `Action ${action} recorded for report in database`,
      report,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

// =========================================================================
// CATEGORIES & SKILLS
// =========================================================================

const createCategory = async (req, res) => {
  try {
    const { name } = req.body;
    if (!name || !name.trim()) {
      return res.status(400).json({ success: false, message: 'Category name is required' });
    }
    const cleanName = name.trim();
    const existing = await Category.findOne({ name: new RegExp(`^${cleanName}$`, 'i') });
    if (existing) {
      return res.status(400).json({ success: false, message: 'Category already exists' });
    }

    const count = await Category.countDocuments();
    const newCat = await Category.create({
      categoryId: `C-${count + 1}`,
      name: cleanName,
      projectCount: 0,
      active: true,
    });

    await logAudit('Jordan Lee', 'JL', 'Created category', cleanName, true);

    return res.status(201).json({
      success: true,
      message: 'Category created in database',
      category: newCat,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

const updateCategory = async (req, res) => {
  try {
    const { id } = req.params;
    const { name, active, oldName } = req.body;

    let cat;
    if (id && id.match(/^[0-9a-fA-F]{24}$/)) {
      cat = await Category.findById(id);
    }
    if (!cat && (oldName || name)) {
      cat = await Category.findOne({ name: new RegExp(`^${(oldName || name).trim()}$`, 'i') });
    }
    if (!cat) {
      return res.status(404).json({ success: false, message: 'Category not found' });
    }

    if (active !== undefined) {
      cat.active = active;
      const act = cat.active ? 'Enabled category' : 'Disabled category';
      await logAudit('Jordan Lee', 'JL', act, cat.name, true);
    }
    if (name && name.trim()) {
      const prev = cat.name;
      cat.name = name.trim();
      await logAudit('Jordan Lee', 'JL', 'Updated category name', `${prev} → ${cat.name}`, true);
    }
    await cat.save();

    return res.status(200).json({
      success: true,
      message: 'Category updated in database',
      category: cat,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

const createSkill = async (req, res) => {
  try {
    const { name } = req.body;
    if (!name || !name.trim()) {
      return res.status(400).json({ success: false, message: 'Skill name is required' });
    }
    const cleanName = name.trim();
    const existing = await SkillTag.findOne({ name: new RegExp(`^${cleanName}$`, 'i') });
    if (existing) {
      return res.status(400).json({ success: false, message: 'Skill already exists' });
    }

    const count = await SkillTag.countDocuments();
    const newSkill = await SkillTag.create({
      skillId: `S-${count + 1}`,
      name: cleanName,
      memberCount: 1,
      active: true,
    });

    await logAudit('Jordan Lee', 'JL', 'Created skill tag', cleanName, true);

    return res.status(201).json({
      success: true,
      message: 'Skill created in database',
      skill: newSkill,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

const updateSkill = async (req, res) => {
  try {
    const { id } = req.params;
    const { name, active, oldName } = req.body;

    let skill;
    if (id && id.match(/^[0-9a-fA-F]{24}$/)) {
      skill = await SkillTag.findById(id);
    }
    if (!skill && (oldName || name)) {
      skill = await SkillTag.findOne({ name: new RegExp(`^${(oldName || name).trim()}$`, 'i') });
    }
    if (!skill) {
      return res.status(404).json({ success: false, message: 'Skill not found' });
    }

    if (active !== undefined) {
      skill.active = active;
      const act = skill.active ? 'Enabled skill tag' : 'Disabled skill tag';
      await logAudit('Jordan Lee', 'JL', act, skill.name, true);
    }
    if (name && name.trim()) {
      const prev = skill.name;
      skill.name = name.trim();
      await logAudit('Jordan Lee', 'JL', 'Updated skill tag name', `${prev} → ${skill.name}`, true);
    }
    await skill.save();

    return res.status(200).json({
      success: true,
      message: 'Skill updated in database',
      skill,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

// =========================================================================
// REVIEWS CRUD
// =========================================================================

const createReview = async (req, res) => {
  try {
    const { reviewer, reviewerName, revieweeName, projectName, roleOrContext, rating, text, reviewText, type } = req.body;
    const revName = reviewerName || reviewer || 'Anonymous';
    const content = reviewText || text || '';
    if (!content.trim()) {
      return res.status(400).json({ success: false, message: 'Review text is required' });
    }

    const count = await Review.countDocuments();
    const newRev = await Review.create({
      reviewId: `REV-${500 + count + 1}`,
      reviewerName: revName.trim(),
      reviewerInitials: computeInitials(revName),
      revieweeName: (revieweeName || 'Alex Morgan').trim(),
      projectName: (projectName || 'Community Project').trim(),
      roleOrContext: (roleOrContext || 'Contributor').trim(),
      rating: Math.max(1, Math.min(5, parseInt(rating, 10) || 5)),
      reviewText: content.trim(),
      reviewType: type || 'Team Member Review',
      date: formatDate(new Date()),
      status: 'Published',
      helpfulVotes: 0,
      verifiedCollaboration: true,
    });

    await logAudit('Jordan Lee', 'JL', 'Added review', `${newRev.reviewId} (${revName})`, true);

    return res.status(201).json({
      success: true,
      message: 'Review created in database',
      review: newRev,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

const updateReview = async (req, res) => {
  try {
    const { id } = req.params;
    const { status, action } = req.body; // 'approve', 'hide', 'unhide'

    let review;
    if (id && id.match(/^[0-9a-fA-F]{24}$/)) {
      review = await Review.findById(id);
    }
    if (!review) {
      review = await Review.findOne({ reviewId: id });
    }
    if (!review) {
      return res.status(404).json({ success: false, message: 'Review not found' });
    }

    if (action === 'approve') {
      review.status = 'Published';
      review.reportReason = null;
      review.reportedBy = null;
      await logAudit('Jordan Lee', 'JL', 'Approved reported feedback', `${review.reviewId} (${review.reviewerName})`, true);
    } else if (action === 'hide' || status === 'Hidden') {
      review.status = 'Hidden';
      await logAudit('Jordan Lee', 'JL', 'Hid feedback', review.reviewId || id, true);
    } else if (action === 'unhide' || status === 'Published') {
      review.status = 'Published';
      await logAudit('Jordan Lee', 'JL', 'Unhid feedback', review.reviewId || id, true);
    }
    await review.save();

    return res.status(200).json({
      success: true,
      message: 'Review status updated in database',
      review,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

const deleteReview = async (req, res) => {
  try {
    const { id } = req.params;

    let review;
    if (id && id.match(/^[0-9a-fA-F]{24}$/)) {
      review = await Review.findById(id);
    }
    if (!review) {
      review = await Review.findOne({ reviewId: id });
    }
    if (!review) {
      return res.status(404).json({ success: false, message: 'Review not found to delete' });
    }

    const rId = review.reviewId || String(review._id);
    const rAuthor = review.reviewerName;
    await Review.findByIdAndDelete(review._id);

    await logAudit('Jordan Lee', 'JL', 'Deleted inappropriate feedback', `${rId} (${rAuthor})`, true);

    return res.status(200).json({
      success: true,
      message: 'Review deleted successfully from database',
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

// =========================================================================
// SETTINGS
// =========================================================================

const updateSettings = async (req, res) => {
  try {
    let settings = await PlatformSetting.findOne({ key: 'global' });
    if (!settings) {
      settings = new PlatformSetting({ key: 'global' });
    }

    // Apply allowed body fields
    const allowed = [
      'platformName', 'contactEmail', 'supportEmail', 'platformLogoText', 'defaultLanguage',
      'allowedDomains', 'strictDomainEnforcement', 'userRegistrationEnabled', 'requireEmailVerification',
      'requireStudentId', 'defaultProfileVisibility', 'requireProjectApproval', 'projectCreationRole',
      'maxProjectsPerStudent', 'autoArchiveInactiveProjects', 'minTeamSize', 'maxTeamSize',
      'allowCrossDepartmentTeams', 'autoLockFullTeams', 'maxApplicationsPerStudent', 'allowApplicationWithdrawal',
      'applicationExpiryDays', 'requireResumeOrPortfolio', 'spamSensitivityThreshold', 'maxLoginAttempts',
      'sessionTimeoutMinutes', 'twoFactorAuthEnforced', 'emailNotificationsEnabled', 'inAppNotificationsEnabled',
      'digestFrequency', 'announcementBroadcasts', 'maintenanceModeActive', 'maintenanceNotice',
      'appearanceTheme', 'accentColorIndex', 'compactMode', 'dataRetentionDays'
    ];

    for (const key of allowed) {
      if (req.body[key] !== undefined) {
        settings[key] = req.body[key];
      }
    }

    await settings.save();
    await logAudit('Jordan Lee', 'JL', 'Updated platform configuration', 'Global Settings', true);

    return res.status(200).json({
      success: true,
      message: 'Platform settings saved to database',
      settings,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

const addDomain = async (req, res) => {
  try {
    const { domain } = req.body;
    if (!domain || !domain.trim()) {
      return res.status(400).json({ success: false, message: 'Domain is required' });
    }
    let clean = domain.trim().toLowerCase();
    if (!clean.startsWith('@')) clean = '@' + clean;

    let settings = await PlatformSetting.findOne({ key: 'global' });
    if (!settings) settings = new PlatformSetting({ key: 'global' });

    if (!settings.universityDomainsList.includes(clean)) {
      settings.universityDomainsList.push(clean);
      settings.allowedDomains = settings.universityDomainsList.join(', ');
      await settings.save();
      await logAudit('Jordan Lee', 'JL', 'Added university domain', clean, true);
    }

    return res.status(200).json({
      success: true,
      message: `Domain ${clean} added to allowed university list`,
      domains: settings.universityDomainsList,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

const removeDomain = async (req, res) => {
  try {
    const { domain } = req.body;
    if (!domain) {
      return res.status(400).json({ success: false, message: 'Domain is required' });
    }
    let clean = domain.trim().toLowerCase();

    let settings = await PlatformSetting.findOne({ key: 'global' });
    if (!settings) settings = new PlatformSetting({ key: 'global' });

    settings.universityDomainsList = settings.universityDomainsList.filter((d) => d !== clean);
    settings.allowedDomains = settings.universityDomainsList.join(', ');
    await settings.save();

    await logAudit('Jordan Lee', 'JL', 'Removed university domain', clean, true);

    return res.status(200).json({
      success: true,
      message: `Domain ${clean} removed`,
      domains: settings.universityDomainsList,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

const changeAdminPassword = async (req, res) => {
  try {
    const { currentPassword, newPassword } = req.body;
    let settings = await PlatformSetting.findOne({ key: 'global' });
    if (!settings) settings = new PlatformSetting({ key: 'global' });

    if (currentPassword !== settings.adminPassword) {
      return res.status(400).json({ success: false, message: 'Current admin password does not match' });
    }
    if (!newPassword || newPassword.trim().length < 6) {
      return res.status(400).json({ success: false, message: 'New password must be at least 6 characters' });
    }

    settings.adminPassword = newPassword.trim();
    await settings.save();

    await logAudit('Jordan Lee', 'JL', 'Changed admin password', 'Security Settings', true);

    return res.status(200).json({
      success: true,
      message: 'Admin password successfully changed in database',
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

const resetSettings = async (req, res) => {
  try {
    await PlatformSetting.deleteOne({ key: 'global' });
    const fresh = await PlatformSetting.create({ key: 'global' });

    await logAudit('Jordan Lee', 'JL', 'Reset platform settings to default', 'Global Configuration', true);

    return res.status(200).json({
      success: true,
      message: 'Platform settings reset to defaults in database',
      settings: fresh,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

module.exports = {
  getAdminBootstrap,
  createUser,
  updateUserRole,
  updateUserStatus,
  deleteUser,
  createProject,
  updateProjectStatus,
  deleteProject,
  resolveReport,
  actionReport,
  createCategory,
  updateCategory,
  createSkill,
  updateSkill,
  createReview,
  updateReview,
  deleteReview,
  updateSettings,
  addDomain,
  removeDomain,
  changeAdminPassword,
  resetSettings,
};

