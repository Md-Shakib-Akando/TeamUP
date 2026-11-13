const express = require('express');
const cors = require('cors');
const path = require('path');
const dotenv = require('dotenv');

// Load environment variables reliably from server/.env, workspace root, or cwd
dotenv.config({ path: path.join(__dirname, '.env') });
dotenv.config({ path: path.join(__dirname, '..', '.env') });
dotenv.config();

const connectDB = require('./src/config/db');

// Connect to MongoDB Atlas
connectDB();

const app = express();

// Middlewares
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Request logging in development
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
  next();
});

// Auto-reconnect to MongoDB Atlas if connection dropped or delayed
app.use(async (req, res, next) => {
  const mongoose = require('mongoose');
  if (mongoose.connection.readyState === 0 && process.env.MONGODB_URI) {
    try {
      await connectDB();
    } catch (e) {
      // Handled in connectDB
    }
  }
  next();
});

// Routes
app.use('/api/auth', require('./src/routes/authRoutes'));
app.use('/api/admin', require('./src/routes/adminRoutes'));
app.use('/api', require('./src/routes/studentRoutes'));

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.status(200).json({
    status: 'online',
    service: 'TeamUp Auth API',
    database: require('mongoose').connection.readyState === 1 ? 'connected' : 'disconnected',
    timestamp: new Date().toISOString(),
  });
});

// Root endpoint
app.get('/', (req, res) => {
  res.send('TeamUp Platform API Server is running.');
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ success: false, message: 'API route not found' });
});

// Global error handler
app.use((err, req, res, next) => {
  console.error('[Server Error]:', err.stack);
  res.status(500).json({
    success: false,
    message: err.message || 'Internal Server Error',
  });
});

const PORT = process.env.PORT || 5050;

app.listen(PORT, '0.0.0.0', () => {
  console.log(`====================================================`);
  console.log(` TeamUp Node.js API Server running on port ${PORT}`);
  console.log(` Health check: http://localhost:${PORT}/api/health`);
  console.log(` Auth endpoints:`);
  console.log(`   POST http://localhost:${PORT}/api/auth/register`);
  console.log(`   POST http://localhost:${PORT}/api/auth/login`);
  console.log(` Student dashboard (JWT): GET http://localhost:${PORT}/api/dashboard`);
  console.log(`====================================================`);
});

module.exports = app;

