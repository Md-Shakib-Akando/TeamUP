/**
 * Display helpers shared by the dashboard API. The Java client renders strings as-is,
 * so dates/labels are formatted server-side relative to the time of the request.
 */
const mongoose = require('mongoose');

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
const MONTHS_LONG = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August',
  'September', 'October', 'November', 'December'];
const DAYS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
const DAY_MS = 24 * 60 * 60 * 1000;

const pad2 = (n) => (n < 10 ? `0${n}` : `${n}`);

const shortDate = (d) => (d ? `${MONTHS[d.getMonth()]} ${pad2(d.getDate())}` : '');
const longDate = (d) => (d ? `${MONTHS[d.getMonth()]} ${pad2(d.getDate())}, ${d.getFullYear()}` : '');
const fullDate = (d) => (d ? `${MONTHS_LONG[d.getMonth()]} ${d.getDate()}, ${d.getFullYear()}` : '');

const startOfDay = (d) => new Date(d.getFullYear(), d.getMonth(), d.getDate());
const dayDiff = (d, now = new Date()) => Math.round((startOfDay(d) - startOfDay(now)) / DAY_MS);

/** "18m", "2h", "3d" — compact form used in the team feed. */
const agoCompact = (d, now = new Date()) => {
  const mins = Math.max(0, Math.round((now - d) / 60000));
  if (mins < 60) return `${Math.max(1, mins)}m`;
  const hrs = Math.round(mins / 60);
  if (hrs < 24) return `${hrs}h`;
  return `${Math.round(hrs / 24)}d`;
};

/** "42 min ago", "1 hour ago", "Yesterday", "3 days ago" */
const agoLong = (d, now = new Date()) => {
  const mins = Math.max(0, Math.round((now - d) / 60000));
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins} min ago`;
  const hrs = Math.round(mins / 60);
  if (hrs < 24) return hrs === 1 ? '1 hour ago' : `${hrs} hours ago`;
  const days = Math.round(hrs / 24);
  if (days === 1) return 'Yesterday';
  if (days < 30) return `${days} days ago`;
  return longDate(d);
};

/** Label for a due date: "Overdue · Oct 15", "Today · Oct 16", "Tomorrow · Oct 17", "Mon, Oct 19". */
const dueLabel = (d, now = new Date()) => {
  if (!d) return { label: 'No due date', statusType: 2 };
  const diff = dayDiff(d, now);
  if (diff < 0) return { label: `Overdue · ${shortDate(d)}`, statusType: 0 };
  if (diff === 0) return { label: `Today · ${shortDate(d)}`, statusType: 1 };
  if (diff === 1) return { label: `Tomorrow · ${shortDate(d)}`, statusType: 2 };
  return { label: `${DAYS[d.getDay()]}, ${shortDate(d)}`, statusType: 2 };
};

const initials = (name = '') => {
  const parts = String(name).trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return 'TU';
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
};

const firstName = (name = '') => String(name).trim().split(/\s+/)[0] || 'there';

/** "University of Washington" -> "UW"; short names are kept as-is. */
const shortUniversity = (u = '') => {
  if (!u) return '';
  if (u.length <= 12) return u;
  const letters = u.split(/\s+/).filter((w) => w && !/^(of|the|and|at)$/i.test(w)).map((w) => w[0].toUpperCase());
  return letters.join('');
};

/** Avatar palettes taken from the Java dashboard's demo cards. */
const AVATAR_PALETTE = [
  ['#EDE9FE', '#6D28D9'],
  ['#DCFCE7', '#15803D'],
  ['#FFE4E6', '#BE123C'],
  ['#FEF3C7', '#B45309'],
  ['#EEF2FF', '#4338CA'],
  ['#CFFAFE', '#0E7490'],
  ['#FCE7F3', '#BE185D'],
  ['#DBEAFE', '#1E40AF'],
];

const paletteFor = (seed = '') => {
  let h = 0;
  for (const ch of String(seed)) h = (h * 31 + ch.charCodeAt(0)) >>> 0;
  return AVATAR_PALETTE[h % AVATAR_PALETTE.length];
};

const isId = (v) => mongoose.Types.ObjectId.isValid(v) && String(new mongoose.Types.ObjectId(v)) === String(v);

module.exports = {
  DAY_MS,
  shortDate,
  longDate,
  fullDate,
  dayDiff,
  agoCompact,
  agoLong,
  dueLabel,
  initials,
  firstName,
  shortUniversity,
  paletteFor,
  isId,
};
