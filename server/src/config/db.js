const mongoose = require('mongoose');
const path = require('path');
const dotenv = require('dotenv');

// Ensure env is loaded even if db.js is imported directly
if (!process.env.MONGODB_URI) {
  dotenv.config({ path: path.join(__dirname, '../../.env') });
  dotenv.config({ path: path.join(__dirname, '../../../.env') });
  dotenv.config();
}

let listenersAttached = false;

/**
 * TeamUp keeps its data in its own database (default "teamup") so it never mixes
 * with other apps that share the same Atlas cluster. Override with MONGODB_DB_NAME.
 */
const connectDB = async () => {
  try {
    const connUri = process.env.MONGODB_URI;
    if (!connUri) {
      console.warn('[MongoDB] WARNING: MONGODB_URI not set in environment or .env file.');
      console.warn('[MongoDB] Please provide your MongoDB Atlas connection string in server/.env');
      return null;
    }

    if (!listenersAttached) {
      listenersAttached = true;
      mongoose.connection.on('connected', () => {
        console.log('[MongoDB Atlas] Connection established.');
      });
      mongoose.connection.on('error', (err) => {
        console.error('[MongoDB Atlas] Connection error event:', err.message);
      });
      mongoose.connection.on('disconnected', () => {
        console.warn('[MongoDB Atlas] Disconnected from database.');
      });
    }

    const conn = await mongoose.connect(connUri, {
      dbName: process.env.MONGODB_DB_NAME || 'teamup',
      serverSelectionTimeoutMS: 8000,
    });

    console.log(`[MongoDB Atlas] Connected: ${conn.connection.host} / db=${conn.connection.name}`);
    return conn;
  } catch (error) {
    console.error(`[MongoDB Atlas] Connection error: ${error.message}`);
    return null;
  }
};

module.exports = connectDB;
