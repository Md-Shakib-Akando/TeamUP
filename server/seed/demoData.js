/**
 * Demo content for the TeamUp student dashboard, transcribed from the hardcoded
 * demo data in src/dashboard/student/*.java so the app shows the same world when
 * it runs against MongoDB.
 *
 * Dates are expressed relative to "now" (days / minutes offsets) so deadlines,
 * activity feeds and "x min ago" labels stay fresh whenever you re-seed.
 */

const DEMO_PASSWORD = 'teamup123';

const SKILL_CATEGORIES = {
  Languages: ['Java', 'Python', 'TypeScript', 'JavaScript', 'C++', 'Rust', 'Swift', 'Dart'],
  Frameworks: ['React', 'Node.js', 'Next.js', 'Vue.js', 'Flutter', 'React Native', 'FastAPI', 'Express',
    'Tailwind', 'Prisma', 'ROS', 'PyTorch'],
  Database: ['PostgreSQL', 'MongoDB', 'Firebase', 'Supabase'],
  Design: ['Figma', 'UX research', 'Design systems', 'UI/UX', 'UI/UX Design', 'User Testing', 'Accessibility', 'Copywriting'],
};
const categoryOf = (skill) =>
  Object.keys(SKILL_CATEGORIES).find((c) => SKILL_CATEGORIES[c].includes(skill)) || 'Tools';

// key, name, headline, university, [avatarBg, avatarFg], availability, rating, reviews,
// skills, matchHint, matchDesc, portfolio, experience, bio
const TEAMMATES = [
  ['priya', 'Priya Shah', 'UX/UI designer', 'University of Washington', ['#EDE9FE', '#6D28D9'], 'Available · 10-15 hrs / week', 4.9, 12,
    ['Figma', 'UX research', 'Design systems'], 96, 'Pairs your full-stack skills with user research and accessible product design.',
    'priyashah.design', 'Advanced', 'Senior studying HCDE at UW. Passionate about human-centered design, prototyping in Figma, and building inclusive digital experiences.'],
  ['daniel', 'Daniel Kim', 'Backend developer', 'University of Washington', ['#DCFCE7', '#15803D'], 'Available · 8-12 hrs / week', 4.8, 9,
    ['Node.js', 'PostgreSQL', 'Docker'], 92, 'Adds API architecture and database expertise to your React projects.',
    'github.com/danielkim', 'Advanced', 'Junior CS student at UW. Focused on backend scalability, concurrent REST/GraphQL APIs, and Docker deployments.'],
  ['sofia', 'Sofia Martinez', 'Frontend developer', 'University of Oregon', ['#FFE4E6', '#BE123C'], 'Available · 10 hrs / week', 4.9, 8,
    ['React', 'TypeScript', 'Accessibility'], 89, 'Shares your TypeScript interests and brings a strong accessibility focus.',
    'sofiam.dev', 'Intermediate', 'Junior studying CS at UO. Loves crafting delightful micro-interactions, responsive CSS layouts, and WCAG AA accessibility.'],
  ['ethan', 'Ethan Chen', 'QA engineer', 'University of Washington', ['#FEF3C7', '#B45309'], 'Available · 6-10 hrs / week', 4.7, 6,
    ['Playwright', 'Testing', 'CI/CD'], 88, 'Complements your development work with automated tests and release quality.',
    'github.com/ethanchen', 'Intermediate', 'Senior Informatics major specializing in test-driven development, GitHub Actions pipelines, and automated end-to-end testing.'],
  ['maya', 'Maya Patel', 'Full-stack developer', 'UC Berkeley', ['#EEF2FF', '#4338CA'], 'Available · 8 hrs / week', 4.8, 11,
    ['React', 'Python', 'Maps API'], 85, 'A shared interest in sustainability, with geospatial skills for new ideas.',
    'mayapatel.dev', 'Advanced', 'Computer Science junior at UC Berkeley. Experienced in Python data pipelines, interactive mapping visualizations, and Next.js.'],
  ['noah', 'Noah Williams', 'Data engineer', 'Stanford University', ['#EEF2FF', '#4F46E5'], 'Available · 5-8 hrs / week', 4.8, 7,
    ['Python', 'PostgreSQL', 'Data science'], 83, 'Brings data modeling and research experience to your product thinking.',
    'github.com/noahw', 'Intermediate', 'Data Science sophomore at Stanford with a strong interest in relational database architecture, ETL workflows, and research data.'],
  ['leila', 'Leila Hassan', 'Mobile Developer', 'University of Washington', ['#FCE7F3', '#BE185D'], 'Available · 10-12 hrs / week', 4.9, 14,
    ['React Native', 'Swift', 'Firebase'], 82, 'Mobile architecture specialist with published iOS and Android campus apps.',
    'leilahassan.io', 'Advanced', 'Passionate about cross-platform native app performance, push notification workflows, and intuitive gesture-driven interfaces.'],
  ['oliver', 'Oliver Brooks', 'Full-stack developer', 'University of Washington', ['#DCF8C6', '#166534'], 'Available · 8-10 hrs / week', 4.8, 10,
    ['Next.js', 'Prisma', 'Tailwind'], 80, 'Rapid prototyper who ships high quality full-stack web applications quickly.',
    'oliverbrooks.tech', 'Intermediate', 'Senior student who loves turning ideas into working SaaS projects overnight using modern serverless platforms and Prisma ORM.'],
  ['aisha', 'Aisha Rahman', 'Product Designer', 'University of Washington', ['#E0E7FF', '#3730A3'], 'Available · 12-15 hrs / week', 5.0, 18,
    ['Figma', 'UI/UX', 'User Testing'], 79, 'Design lead with multiple university design competition winning entries.',
    'aisharahman.design', 'Advanced', "Master's student in Interaction Design. Passionate about design systems, usability benchmarking, and high-fidelity clickable mockups."],
  ['marcus', 'Marcus Chen', 'Cloud & DevOps', 'UC Berkeley', ['#FEF3C7', '#92400E'], 'Available · 6-8 hrs / week', 4.7, 8,
    ['Docker', 'Kubernetes', 'AWS'], 78, 'Automates container pipelines, CI deployments, and cloud infrastructure.',
    'marcuschen.cloud', 'Advanced', 'Specialized in AWS cloud solutions, Docker orchestration, and zero-downtime deployment pipelines for academic tech teams.'],
  ['sarah', 'Sarah Jenkins', 'Frontend engineer', 'University of Washington', ['#FFEDD5', '#C2410C'], 'Available · 6-8 hrs / week', 4.8, 9,
    ['Vue.js', 'Tailwind', 'CSS'], 76, 'Component-driven frontend design with responsive micro-animations.',
    'sarahj.dev', 'Beginner', 'Sophomore student passionate about semantic web technologies, modern reactive state management, and lightweight animations.'],
  ['chloe', 'Chloe Bennett', 'UX Researcher', 'Stanford University', ['#CFFAFE', '#0E7490'], 'Available · 8-12 hrs / week', 4.9, 11,
    ['UX research', 'Figma', 'User Testing'], 75, 'Conducts student user interviews and synthesizes qualitative insights.',
    'chloeb.design', 'Intermediate', 'Enjoys user interviews, card sorting, wireframe usability tests, and turning student feedback into concrete feature roadmaps.'],
  ['lucas', 'Lucas Vance', 'Systems Engineer', 'University of Washington', ['#DBEAFE', '#1E40AF'], 'Available · 5-8 hrs / week', 4.8, 6,
    ['C++', 'Rust', 'Linux'], 74, 'Low-level systems architecture and high-throughput backend services.',
    'github.com/lucasv', 'Advanced', 'CS senior focused on performance profiling, multithreaded concurrency, and memory-safe systems software in Rust and C++.'],
  ['fatima', 'Fatima Zahra', 'Mobile Developer', 'University of Washington', ['#F3E8FF', '#6B21A8'], 'Available · 10 hrs / week', 4.9, 13,
    ['Flutter', 'Firebase', 'Dart'], 73, 'Cross-platform mobile apps for Android and iOS student workflows.',
    'fatimaz.dev', 'Intermediate', 'Experienced Flutter developer who enjoys creating clean state architecture with Bloc and real-time synchronization with Firestore.'],
  ['ryan', 'Ryan Cooper', 'DevOps & Cloud', 'UCLA', ['#FFEDD5', '#9A3412'], 'Available · 6-10 hrs / week', 4.7, 7,
    ['Docker', 'CI/CD', 'AWS'], 72, 'CI/CD pipeline automation and cloud container deployments.',
    'github.com/ryancooper', 'Intermediate', 'Focuses on developer experience, reproducible container builds, automated unit test runners, and monitoring alerts.'],
  ['emma', 'Emma Wilson', 'Data Scientist', 'University of Washington', ['#CCFBF1', '#115E59'], 'Available · 8-10 hrs / week', 4.8, 12,
    ['Python', 'PostgreSQL', 'Data science'], 70, 'Statistical analysis and predictive modeling for academic research.',
    'emmawilson.data', 'Intermediate', 'Statistics & CS double major. Skilled in Pandas, Scikit-learn, exploratory data analysis, and dashboard communication.'],
  ['liam', 'Liam Davis', 'Security & QA', 'University of Oregon', ['#EEF2FF', '#3730A3'], 'Available · 6-8 hrs / week', 4.7, 5,
    ['Testing', 'Python', 'Security'], 69, 'Web application penetration testing and secure authentication review.',
    'liamdavis.security', 'Intermediate', 'Passionate about web app vulnerabilities (OWASP Top 10), cryptographic protocols, and secure credential handling in student teams.'],
  ['zoe', 'Zoe Martinez', 'Content & UI', 'University of Washington', ['#DCFCE7', '#166534'], 'Available · 8-12 hrs / week', 4.8, 9,
    ['Figma', 'UI/UX', 'Copywriting'], 68, 'Brand voice, onboarding flows, and polished design systems.',
    'zoemartinez.design', 'Beginner', 'Communications and Design double major who writes clear product copy and crafts approachable user onboarding flows.'],
  ['ethanh', 'Ethan Hunt', 'Robotics Engineer', 'Stanford University', ['#EEF2FF', '#4338CA'], 'Available · 10-15 hrs / week', 4.9, 16,
    ['C++', 'ROS', 'Python'], 67, 'Sensor fusion and robotics algorithms for capstone hardware.',
    'github.com/ethanhunt', 'Advanced', 'Robotics enthusiast experienced with LiDAR mapping, SLAM, motor controllers, and Linux embedded computing.'],
  ['mayalin', 'Maya Lin', 'Bioinformatics', 'UC Berkeley', ['#F3E8FF', '#7E22CE'], 'Available · 8 hrs / week', 4.8, 8,
    ['Python', 'PostgreSQL', 'Data science'], 66, 'Genomic data pipelines and molecular modeling simulations.',
    'mayalin.bio', 'Advanced', 'Computational Biology researcher with experience handling high-throughput DNA sequencing data with Python and biopython.'],
  ['david', 'David Park', 'Full-stack developer', 'University of Washington', ['#DBEAFE', '#1D4ED8'], 'Available · 10-14 hrs / week', 4.9, 11,
    ['React', 'TypeScript', 'Node.js'], 65, 'High performance microservices with modern React frontends.',
    'davidpark.codes', 'Advanced', 'Software engineer with an emphasis on type safety, clean code architecture, and resilient API microservices.'],
  ['grace', 'Grace Hopper', 'Backend engineer', 'University of Washington', ['#DCFCE7', '#15803D'], 'Available · 8-10 hrs / week', 5.0, 20,
    ['Java', 'PostgreSQL', 'Docker'], 64, 'Robust Java backend services and concurrent database schemas.',
    'gracehopper.dev', 'Advanced', 'Senior computer scientist who loves clean OOP design, enterprise Java frameworks, and optimized SQL indices.'],
  ['james', 'James Wilson', 'Frontend developer', 'University of Washington', ['#FEF3C7', '#B45309'], 'Available · 6-8 hrs / week', 4.6, 5,
    ['React', 'JavaScript', 'CSS'], 62, 'Clean semantic markup and accessible UI components.',
    'github.com/jwilson', 'Beginner', 'Sophomore student actively learning React, state management, and clean modern responsive layouts.'],
  ['elena', 'Elena Rostova', 'AI Research', 'Stanford University', ['#FCE7F3', '#9D174D'], 'Available · 8-12 hrs / week', 4.9, 12,
    ['Python', 'FastAPI', 'Data science'], 60, 'Natural language processing and sentiment analysis pipelines.',
    'elenarostova.ai', 'Advanced', 'Graduate student working on Transformer models, LLM prompt orchestration, and ethical AI evaluation frameworks.'],
  // Project creators referenced by Discover Projects who are not on the teammate list
  ['alexr', 'Alex Rivera', 'Robotics researcher', 'University of Washington', ['#DBEAFE', '#3B82F6'], 'Available · 6-8 hrs / week', 4.7, 6,
    ['ROS', 'C++', 'Python'], 58, 'Autonomous navigation research for assistive campus robots.',
    'github.com/alexrivera', 'Advanced', 'Graduate researcher building indoor navigation stacks for assistive delivery robots.'],
  ['elenav', 'Elena Vance', 'Mobile & wellbeing', 'University of Oregon', ['#F3E8FF', '#8B5CF6'], 'Available · 5-8 hrs / week', 4.8, 7,
    ['Flutter', 'Node.js', 'MongoDB'], 57, 'Builds privacy-first mobile experiences for student wellbeing.',
    'elenavance.app', 'Intermediate', 'Psychology & CS student creating anonymous peer-support tools with licensed counselor triage.'],
  ['amara', 'Amara Okafor', 'QA engineer', 'University of Washington', ['#FFE4E6', '#BE123C'], 'Available · 8-10 hrs / week', 4.8, 6,
    ['Playwright', 'Testing', 'TypeScript'], 81, 'End-to-end test automation to keep your releases stable.',
    'github.com/amaraokafor', 'Intermediate', 'Junior in Informatics who enjoys writing Playwright suites and hunting flaky tests.'],
];

/** The demo persona everything in the original Java dashboard was written for. */
const PERSONA = {
  key: 'alex',
  name: 'Alex Morgan',
  email: 'alex.morgan@example.com',
  username: 'alexmorgan',
  headline: 'Full-Stack Developer & UI/UX Enthusiast',
  university: 'University of Washington',
  department: 'Paul G. Allen School of Computer Science',
  gradYear: 'Class of 2027',
  location: 'Seattle, WA · Central Time (UTC-05:00)',
  github: 'github.com/alexmorgan',
  portfolio: 'alexmorgan.dev',
  bio: 'Building software that bridges design and engineering. Passionate about campus sustainability, real-time team collaboration tools, and intuitive human-centered interfaces.',
  availability: 'Available for new teams (10–15 hrs/wk)',
  experience: 'Advanced',
  avatar: ['#EDE9FE', '#6D28D9'],
  rating: 4.9,
  reviews: 9, // + 3 real Review documents below = 12
  memberSinceDaysAgo: 246,
  skills: [
    ['Java', 'Languages', 18], ['React', 'Frameworks', 15], ['TypeScript', 'Languages', 14],
    ['UI/UX Design', 'Design', 12], ['Node.js', 'Frameworks', 11], ['PostgreSQL', 'Database', 9],
    ['Figma', 'Design', 13], ['REST APIs', 'Tools', 16],
  ],
  settings: {
    visibility: 'PUBLIC',
    showSkills: true,
    showActivity: false,
    categories: ['Web Development', 'Sustainability', 'AI & Machine Learning'],
    roleFrontendDev: true,
    roleUiDev: true,
    roleBackendDev: false,
    roleProjectLead: false,
    weeklyAvailability: '10–15 hours per week',
    preferredCommitment: '1–3 months',
    collabMode: 'REMOTE',
  },
};

// Discover Projects (DashboardProjects.java) merged with My Projects (DashboardMyProjects.java).
// members: [key, role] — remaining seats up to `current` are filled automatically from the pool.
const PROJECTS = [
  {
    key: 'campusconnect', title: 'CampusConnect', category: 'Campus life', experience: 'Intermediate',
    description: 'One place to discover university events, meet people and make campus feel connected.',
    skills: ['React', 'TypeScript', 'Node.js', 'Tailwind'], status: 'Recruiting', current: 5, max: 7,
    openRoles: ['Frontend developer', 'QA engineer'], owner: 'alex', icon: [0, '#FDEEF4', '#EA4C89'],
    members: [['alex', 'Project Lead & Full-Stack Developer'], ['priya', 'UX Designer'], ['daniel', 'Backend Dev'],
      ['sofia', 'Frontend Dev'], ['ethan', 'QA Engineer']],
    milestones: [
      ['Research & design', -32, 'Completed', 100],
      ['MVP development', 21, 'In progress', 68],
      ['Beta testing & launch', 35, 'Upcoming', 0],
    ],
    files: [
      ['campus-wireframes-v3.fig', 'Figma', '14.2 MB', -2 / 24, 'Priya Shah'],
      ['api-specs-rest-v1.yaml', 'YAML', '420 KB', -1, 'Daniel Kim'],
      ['prd-campusconnect-final.pdf', 'PDF', '2.8 MB', -3, 'Alex Morgan'],
    ],
    lastSyncDays: -2, nextSyncDays: 5,
  },
  {
    key: 'greenroute', title: 'GreenRoute', category: 'Sustainability', experience: 'Intermediate',
    description: 'Make low-carbon campus travel easier with shared routes, micro-mobility, and real-time transit insights.',
    skills: ['React', 'Maps API', 'Node.js', 'Express'], status: 'Active', current: 4, max: 6,
    openRoles: ['Frontend developer', 'Data analyst'], owner: 'maya', icon: [1, '#EBFBF5', '#10B981'],
    members: [['maya', 'Project Lead'], ['alex', 'Frontend & Maps Developer'], ['noah', 'Data Pipeline'], ['chloe', 'UX Researcher']],
    milestones: [['Pilot with campus shuttle routes', 10, 'In progress', 60], ['Public launch', 40, 'Upcoming', 0]],
    lastSyncDays: -1, nextSyncDays: 6,
  },
  {
    key: 'openlab', title: 'OpenLab', category: 'Research', experience: 'Intermediate',
    description: 'Help students discover research opportunities and collaborate across university labs.',
    skills: ['TypeScript', 'PostgreSQL', 'Figma'], status: 'Recruiting', current: 3, max: 5,
    openRoles: ['Product designer', 'Backend developer'], owner: 'noah', icon: [2, '#F3F0FF', '#7C5CFF'],
  },
  {
    key: 'accessmap', title: 'AccessMap', category: 'Social impact', experience: 'Intermediate',
    description: 'Build a community-powered map of accessible spaces and routes around campus.',
    skills: ['React Native', 'Maps API'], status: 'Recruiting', current: 3, max: 5,
    openRoles: ['Mobile developer', 'UX researcher'], owner: 'leila', icon: [3, '#E0F2FE', '#0EA5E9'],
  },
  {
    key: 'peerprep', title: 'AI PeerPrep', category: 'Education', experience: 'Intermediate',
    description: 'Turn interview practice into a shared learning experience with peer feedback, mock interviews, and AI scoring.',
    skills: ['Next.js', 'Prisma', 'FastAPI', 'Python'], status: 'Active', current: 3, max: 4,
    openRoles: ['Full-stack developer', 'Product designer'], owner: 'alex', icon: [4, '#EEF2FF', '#4F46E5'],
    members: [['alex', 'Project Lead & AI Engineer'], ['oliver', 'Backend Dev'], ['fatima', 'Frontend Dev']],
    milestones: [['Mock interview MVP', 12, 'In progress', 40]],
  },
  {
    key: 'foodloop', title: 'FoodLoop', category: 'Sustainability', experience: 'Intermediate',
    description: 'Connect campus cafés with students to reduce food waste, one shared meal at a time.',
    skills: ['React', 'Node.js', 'Figma'], status: 'Recruiting', current: 4, max: 6,
    openRoles: ['Frontend developer', 'QA engineer'], owner: 'aisha', icon: [5, '#F0FDFA', '#0D9488'],
  },
  {
    key: 'studysync', title: 'StudySync', category: 'Education', experience: 'Beginner',
    description: 'Collaborative note-sharing and AI-powered study group matcher for STEM classes.',
    skills: ['Python', 'FastAPI', 'React'], status: 'Recruiting', current: 3, max: 5,
    openRoles: ['Python backend', 'UI designer'], owner: 'marcus', icon: [4, '#EEF2FF', '#4F46E5'],
  },
  {
    key: 'skillswap', title: 'SkillSwap', category: 'Campus life', experience: 'Intermediate',
    description: 'Peer tutoring exchange platform where students trade coding tips for languages.',
    skills: ['Vue.js', 'Firebase', 'Tailwind'], status: 'Recruiting', current: 2, max: 4,
    openRoles: ['Full-stack engineer', 'Community lead'], owner: 'sarah', icon: [0, '#FEF3C7', '#D97706'],
  },
  {
    key: 'solarcampus', title: 'SolarCampus', category: 'Sustainability', experience: 'Advanced',
    description: 'IoT sensor monitoring of rooftop solar panels across engineering facilities.',
    skills: ['Python', 'C++', 'PostgreSQL'], status: 'Recruiting', current: 4, max: 5,
    openRoles: ['Embedded systems', 'Data pipeline'], owner: 'daniel', icon: [1, '#ECFDF5', '#10B981'],
  },
  {
    key: 'mentalwell', title: 'MentalWell', category: 'Social impact', experience: 'Intermediate',
    description: 'Anonymous peer mental health reflection journaling and licensed counselor triage.',
    skills: ['Flutter', 'Node.js', 'MongoDB'], status: 'Recruiting', current: 2, max: 4,
    openRoles: ['Mobile developer', 'UX researcher'], owner: 'elenav', icon: [3, '#F3E8FF', '#8B5CF6'],
  },
  {
    key: 'robonav', title: 'RoboNav', category: 'Research', experience: 'Advanced',
    description: 'Autonomous indoor navigation algorithms for assistive campus delivery robots.',
    skills: ['ROS', 'C++', 'Python'], status: 'Recruiting', current: 3, max: 6,
    openRoles: ['Robotics engineer', 'Computer vision'], owner: 'alexr', icon: [2, '#EEF2FF', '#3B82F6'],
  },
  {
    key: 'ecodine', title: 'EcoDine', category: 'Sustainability', experience: 'Beginner',
    description: 'Gamified reusable container return system for university dining halls.',
    skills: ['React', 'Tailwind', 'Supabase'], status: 'Recruiting', current: 3, max: 4,
    openRoles: ['Frontend engineer', 'Content creator'], owner: 'chloe', icon: [5, '#ECFDF5', '#059669'],
  },
  {
    key: 'coursecritic', title: 'CourseCritic', category: 'Education', experience: 'Intermediate',
    description: 'Syllabus breakdown and workload transparent reviews written by verified alumni.',
    skills: ['Next.js', 'PostgreSQL', 'Prisma'], status: 'Recruiting', current: 4, max: 5,
    openRoles: ['Full-stack engineer', 'Marketing'], owner: 'lucas', icon: [4, '#EEF2FF', '#4F46E5'],
  },
  {
    key: 'fairride', title: 'FairRide', category: 'Campus life', experience: 'Intermediate',
    description: 'Carpooling network connecting suburban commuters with campus parking permit holders.',
    skills: ['React Native', 'Firebase', 'Maps API'], status: 'Recruiting', current: 3, max: 5,
    openRoles: ['Mobile developer', 'UI/UX designer'], owner: 'fatima', icon: [0, '#FFEDD5', '#EA580C'],
  },
  {
    key: 'quantumsim', title: 'QuantumSim', category: 'Research', experience: 'Advanced',
    description: 'Interactive web visualizer for quantum computing logic gates and entanglement.',
    skills: ['TypeScript', 'WebGL', 'Python'], status: 'Recruiting', current: 2, max: 4,
    openRoles: ['WebGL engineer', 'Quantum physics'], owner: 'noah', icon: [2, '#F3E8FF', '#7C3AED'],
  },
  {
    key: 'soundscape', title: 'SoundScape', category: 'Social impact', experience: 'Intermediate',
    description: 'Crowdsourced campus quiet study space decibel heatmaps and sensory map.',
    skills: ['Swift', 'CoreAudio', 'Node.js'], status: 'Recruiting', current: 3, max: 5,
    openRoles: ['iOS developer', 'Audio engineer'], owner: 'ryan', icon: [3, '#ECFEFF', '#0891B2'],
  },
  {
    key: 'bookloop', title: 'BookLoop', category: 'Campus life', experience: 'Beginner',
    description: 'Textbook trade-in and lending library eliminating out-of-pocket course materials cost.',
    skills: ['React', 'Node.js', 'Express'], status: 'Recruiting', current: 4, max: 6,
    openRoles: ['Frontend engineer', 'Community lead'], owner: 'emma', icon: [0, '#ECFDF5', '#059669'],
  },
  {
    key: 'bioinsight', title: 'BioInsight', category: 'Research', experience: 'Advanced',
    description: 'Deep learning models for classifying microscope slide anomalies in biology labs.',
    skills: ['PyTorch', 'Python', 'FastAPI'], status: 'Recruiting', current: 2, max: 4,
    openRoles: ['ML engineer', 'Bioinformatics'], owner: 'david', icon: [2, '#FDE8E8', '#E02424'],
  },
  {
    key: 'solariot', title: 'SolarCampus IoT', category: 'Sustainability', experience: 'Advanced',
    description: 'IoT sensor monitoring of rooftop solar panels across engineering facilities with automated yield forecasting.',
    skills: ['Python', 'PostgreSQL', 'Grafana', 'C++'], status: 'Completed', archived: true, current: 3, max: 5,
    openRoles: [], owner: 'daniel', icon: [1, '#ECFDF5', '#10B981'],
    nextMilestone: 'Project completed & deployed to College of Engineering',
    members: [['daniel', 'Lead'], ['alex', 'Data Pipeline Engineer'], ['lucas', 'Embedded']],
  },
];

// [project, title, dueInDays, status, assignee]
const TASKS = [
  ['campusconnect', 'Finalize event API contract with backend team', -1, 'in_progress', 'alex'],
  ['campusconnect', 'Build event discovery cards and filter chips', 0, 'in_progress', 'alex'],
  ['campusconnect', 'Accessibility and screen reader audit', 3, 'todo', 'alex'],
  ['campusconnect', 'Event RSVP push notifications', 6, 'todo', 'sofia'],
  ['campusconnect', 'Integration tests for event search', 5, 'in_review', 'ethan'],
  ['campusconnect', 'Campus map embed on event page', 4, 'in_review', 'sofia'],
  ['campusconnect', 'Set up PostgreSQL Prisma migration', -4, 'done', 'daniel'],
  ['campusconnect', 'Design high-fidelity Figma mobile mockups', -6, 'done', 'priya'],
  ['campusconnect', 'User interviews with 15 students', -20, 'done', 'priya'],
  ['campusconnect', 'Event schema & REST endpoints', -2, 'done', 'daniel'],
  ['campusconnect', 'Auth flow with campus SSO', -10, 'done', 'alex'],
  ['campusconnect', 'Set up CI pipeline', -12, 'done', 'ethan'],
  ['greenroute', 'Integrate campus shuttle GPS tracking layer', 3, 'in_progress', 'alex'],
  ['greenroute', 'Implement carbon reduction gamification badges', 8, 'todo', 'noah'],
  ['greenroute', 'Weekly carbon savings report', 5, 'todo', 'maya'],
  ['greenroute', 'Route optimization algorithms in Mapbox', -2, 'done', 'alex'],
  ['greenroute', 'User testing session with 12 UW commuters', -8, 'done', 'chloe'],
  ['peerprep', 'Set up Whisper speech-to-text API microservice', 4, 'in_progress', 'oliver'],
  ['peerprep', 'Build real-time peer video room using WebRTC', 9, 'todo', 'alex'],
  ['peerprep', 'Integrate audio transcription & feedback engine', 6, 'todo', 'fatima'],
  ['peerprep', 'Create rubrics for behavioral question evaluations', -1, 'done', 'alex'],
  ['solariot', 'Publish capstone project report and open-source repo', -44, 'done', 'daniel'],
  ['solariot', 'Deploy 16 wireless sensor nodes across Wilcox Hall', -54, 'done', 'alex'],
];

// Pending join requests (shown to the lead as candidates): [project, user, role, matchPct, daysAgo]
const JOIN_REQUESTS = [
  ['campusconnect', 'aisha', 'Product Designer', 79, 1],
  ['campusconnect', 'james', 'Frontend developer', 74, 2],
  ['campusconnect', 'amara', 'QA engineer', 81, 3],
  ['peerprep', 'elena', 'AI Research', 94, 1],
  ['openlab', 'alex', 'Backend developer', 86, 2], // sent by the persona
];

// [from, to, kind]
const INVITATIONS = [
  ['alex', 'leila', 'invite'],
  ['grace', 'alex', 'collab'],
];

// [project, actor, type, text, minutesAgo]
const ACTIVITIES = [
  ['campusconnect', 'priya', 'design', 'Shared event wireframes', 18],
  ['campusconnect', 'daniel', 'completed', 'Completed the event API schema', 42],
  ['campusconnect', 'sofia', 'task', 'Mentioned you in "Build event cards"', 60],
  ['greenroute', 'maya', 'task', 'Updated the shuttle GPS layer spec', 180],
  ['peerprep', 'oliver', 'file', 'Uploaded mock interview rubric', 360],
  ['campusconnect', 'ethan', 'task', 'Opened 3 QA bugs on event search', 300],
  ['greenroute', 'chloe', 'file', 'Uploaded commuter interview notes', 1500],
];

// [text, minutesAgo]
const PERSONA_NOTIFICATIONS = [
  ['Priya Shah accepted your invite to CampusConnect', 10],
  ['Daniel Kim completed the event API schema', 42],
  ['New teammate match found: Sofia Martinez', 60],
  ['Deadline reminder: Finalize API contract', 1440],
];

// Reviews about the persona: [reviewer, project, rating, feedback, daysAgo]
const REVIEWS = [
  ['priya', 'campusconnect', 5, 'Alex has exceptional frontend engineering skill and always respects the design specs down to the pixel. Fantastic teammate to collaborate with!', 26],
  ['daniel', 'campusconnect', 5, 'Clear communication, robust architecture, and always delivers sprint tasks on time. Made API integrations completely painless.', 30],
  ['maya', 'greenroute', 5, 'Alex solved our map rendering performance bottle-necks in record time. Highly recommended for any engineering team!', 46],
];

// What an existing (real) account gets when attached with --attach / --attach-all
const ATTACH = {
  memberships: [['campusconnect', 'Contributor'], ['greenroute', 'Contributor']],
  tasks: [
    ['campusconnect', 'Review onboarding flow copy', 1, 'todo'],
    ['campusconnect', 'Fix event card spacing on mobile', 2, 'in_progress'],
    ['campusconnect', 'Write README for the event API', 5, 'todo'],
    ['greenroute', 'Collect feedback from 5 commuters', 4, 'todo'],
  ],
  notifications: [
    ['Welcome to TeamUp! You were added to CampusConnect', 5],
    ['Priya Shah shared event wireframes', 18],
    ['Maya Patel added you to GreenRoute', 120],
  ],
};

module.exports = {
  DEMO_PASSWORD,
  categoryOf,
  TEAMMATES,
  PERSONA,
  PROJECTS,
  TASKS,
  JOIN_REQUESTS,
  INVITATIONS,
  ACTIVITIES,
  PERSONA_NOTIFICATIONS,
  REVIEWS,
  ATTACH,
};
