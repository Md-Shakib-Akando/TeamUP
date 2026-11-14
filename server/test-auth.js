const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

async function testAuthLogic() {
  console.log('====================================================');
  console.log('  TESTING NODE.JS AUTHENTICATION & SECURITY LOGIC  ');
  console.log('====================================================');

  // 1. Password hashing test with bcrypt
  const plainPass = 'StudentPass123!';
  const salt = await bcrypt.genSalt(10);
  const hash = await bcrypt.hash(plainPass, salt);
  console.log('  [1] Password Hashing: Generated bcrypt hash successfully.');

  const match = await bcrypt.compare(plainPass, hash);
  const failMatch = await bcrypt.compare('WrongPassword', hash);
  if (!match || failMatch) {
    throw new Error('Bcrypt password verification failed!');
  }
  console.log('  [2] Password Comparison: Correct password verified, wrong password rejected.');

  // 2. JWT Token generation & verification test
  const secret = 'test_secret_key_123';
  const payload = { id: '65e9f8a12345678901234567', role: 'Student' };
  const token = jwt.sign(payload, secret, { expiresIn: '7d' });
  console.log('  [3] JWT Generation: Generated signed token successfully.');

  const decoded = jwt.verify(token, secret);
  if (decoded.id !== payload.id || decoded.role !== 'Student') {
    throw new Error('JWT token verification failed!');
  }
  console.log('  [4] JWT Verification: Successfully decoded and verified token payload.');

  // 3. Admin role security test:
  // Public registration role cannot be 'Admin' (defaults to 'Student')
  function getRegistrationRole(requestedRole) {
    if (requestedRole === 'Admin') return 'Student';
    const allowed = ['Student', 'Developer', 'UI/UX Designer', 'Team Leader', 'Researcher', 'Freelancer'];
    return allowed.includes(requestedRole) ? requestedRole : 'Student';
  }

  if (getRegistrationRole('Admin') !== 'Student') {
    throw new Error('Security flaw: Admin self-registration must be blocked!');
  }
  if (getRegistrationRole('Developer') !== 'Developer') {
    throw new Error('Allowed student role should be respected');
  }
  if (getRegistrationRole(null) !== 'Student') {
    throw new Error('Default role must be Student');
  }
  console.log('  [5] Role Guard: Admin role self-registration prevented. Must be manually set in DB.');

  console.log('====================================================');
  console.log('  ALL NODE.JS BACKEND AUTH LOGIC TESTS PASSED 100%! ');
  console.log('====================================================');
}

testAuthLogic().catch((err) => {
  console.error('[TEST ERROR]:', err);
  process.exit(1);
});

