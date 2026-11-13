const mongoose = require('mongoose');
const jwt = require('jsonwebtoken');
const User = require('../models/User');

const waitForDB = async (timeoutMs = 5000) => {
  if (mongoose.connection.readyState === 1) return true;
  if (mongoose.connection.readyState === 0) {
    const connectDB = require('../config/db');
    connectDB();
  }
  const start = Date.now();
  while (mongoose.connection.readyState !== 1 && (Date.now() - start < timeoutMs)) {
    await new Promise(resolve => setTimeout(resolve, 150));
  }
  return mongoose.connection.readyState === 1;
};

// Generate JWT Token
const generateToken = (id) => {
  return jwt.sign({ id }, process.env.JWT_SECRET || 'teamup_default_secret_key_2026', {
    expiresIn: process.env.JWT_EXPIRES_IN || '30d',
  });
};

/**
 * @route   POST /api/auth/register
 * @desc    Register a new user (defaults to 'Student')
 * @access  Public
 */
const register = async (req, res) => {
  try {
    const isReady = await waitForDB(5000);
    if (!isReady) {
      return res.status(503).json({
        success: false,
        message: 'Database is currently connecting to MongoDB Atlas. Please try again in a moment.',
      });
    }

    const { name, email, password, role } = req.body;

    if (!name || !name.trim()) {
      return res.status(400).json({ success: false, message: 'Please enter your full name.' });
    }

    if (!email || !email.trim()) {
      return res.status(400).json({ success: false, message: 'Please enter your email address.' });
    }

    if (!password || password.length < 6) {
      return res.status(400).json({ success: false, message: 'Password must be at least 6 characters.' });
    }

    const cleanEmail = email.trim().toLowerCase();

    // Check if user already exists
    const userExists = await User.findOne({ email: cleanEmail });
    if (userExists) {
      return res.status(400).json({ success: false, message: 'An account with this email already exists.' });
    }

    // Role protection: Admin role CANNOT be assigned via public registration.
    // It must be granted manually in MongoDB Atlas database.
    let userRole = 'Student';
    if (role && role !== 'Admin') {
      const allowedRoles = ['Student', 'Developer', 'UI/UX Designer', 'Team Leader', 'Researcher', 'Freelancer'];
      if (allowedRoles.includes(role)) {
        userRole = role;
      }
    }

    // Create user in MongoDB Atlas
    const user = await User.create({
      name: name.trim(),
      email: cleanEmail,
      password,
      role: userRole,
    });

    const token = generateToken(user._id);

    return res.status(201).json({
      success: true,
      message: `Account created! Welcome, ${user.name}.`,
      user: user.toSafeObject(),
      name: user.name,
      role: user.role,
      token,
    });
  } catch (error) {
    console.error('[Auth Register Error]:', error);
    if (error.code === 11000) {
      return res.status(400).json({
        success: false,
        message: 'An account with this email already exists.',
      });
    }
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error during registration.',
    });
  }
};

/**
 * @route   POST /api/auth/login
 * @desc    Authenticate user & return JWT (Admin role recognized if set in DB)
 * @access  Public
 */
const login = async (req, res) => {
  try {
    const isReady = await waitForDB(5000);
    if (!isReady) {
      return res.status(503).json({
        success: false,
        message: 'Database is currently connecting to MongoDB Atlas. Please try again in a moment.',
      });
    }

    const { email, password } = req.body;

    if (!email || !email.trim()) {
      return res.status(400).json({ success: false, message: 'Please enter your email address.' });
    }

    if (!password) {
      return res.status(400).json({ success: false, message: 'Please enter your password.' });
    }

    const cleanEmail = email.trim().toLowerCase();

    // Find user by email
    const user = await User.findOne({ email: cleanEmail });
    if (!user) {
      return res.status(401).json({ success: false, message: 'No account found with this email.' });
    }

    // Check account status
    if (user.status === 'Suspended') {
      return res.status(403).json({
        success: false,
        message: 'Your account has been suspended by an administrator.',
      });
    }

    // Check password
    const isMatch = await user.matchPassword(password);
    if (!isMatch) {
      return res.status(401).json({ success: false, message: 'Incorrect password. Please try again.' });
    }

    const token = generateToken(user._id);

    return res.status(200).json({
      success: true,
      message: `Welcome back, ${user.name}!`,
      user: user.toSafeObject(),
      name: user.name,
      role: user.role,
      token,
    });
  } catch (error) {
    console.error('[Auth Login Error]:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error during authentication.',
    });
  }
};

/**
 * @route   GET /api/auth/me
 * @desc    Get current logged in user details
 * @access  Private
 */
const getMe = async (req, res) => {
  try {
    const user = await User.findById(req.user._id).select('-password');
    return res.status(200).json({
      success: true,
      user,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

/**
 * @route   GET /api/auth/users
 * @desc    List all users (Admin view)
 * @access  Public/Private
 */
const listUsers = async (req, res) => {
  try {
    const users = await User.find({}).select('-password').sort({ createdAt: -1 });
    return res.status(200).json({
      success: true,
      count: users.length,
      users,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

/**
 * @route   POST /api/auth/change-password
 * @desc    Change current user's password
 * @access  Private
 */
const changePassword = async (req, res) => {
  try {
    const { currentPassword, newPassword } = req.body;
    if (!newPassword || newPassword.length < 6) {
      return res.status(400).json({ success: false, message: 'New password must be at least 6 characters.' });
    }
    const user = await User.findById(req.user._id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found.' });
    }
    if (currentPassword) {
      const isMatch = await user.matchPassword(currentPassword);
      if (!isMatch) {
        return res.status(400).json({ success: false, message: 'Current password is incorrect.' });
      }
    }
    user.password = newPassword;
    await user.save();
    return res.status(200).json({ success: true, message: 'Password changed successfully.' });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message || 'Error changing password.' });
  }
};

module.exports = {
  register,
  login,
  getMe,
  listUsers,
  changePassword,
};

