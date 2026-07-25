import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const navLinks = [
  { to: '/dashboard', label: 'Home', icon: '🏠' },
  { to: '/crisis', label: 'Crisis Help', icon: '🆘' },
  { to: '/checkin', label: 'Check-in', icon: '📋' },
  { to: '/scripts', label: 'Scripts', icon: '📝' },
  { to: '/resources', label: 'Resources', icon: '📚' },
];

const caregiverLinks = [
  { to: '/dashboard', label: 'Home', icon: '🏠' },
  { to: '/caregiver', label: 'Caregiver Guide', icon: '🤝' },
  { to: '/resources', label: 'Resources', icon: '📚' },
];

export default function Navbar() {
  const { user, logoutUser } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logoutUser();
    navigate('/login');
  };

  const links = user?.role === 'CAREGIVER' ? caregiverLinks : navLinks;

  return (
    <nav className="bg-white border-b border-gray-100 shadow-sm sticky top-0 z-50">
      <div className="max-w-6xl mx-auto px-4 py-3 flex items-center justify-between">
        {/* Logo */}
        <Link to="/dashboard" className="flex items-center gap-2">
          <span className="text-2xl">🌿</span>
          <span className="font-bold text-primary-700 text-lg">RecoverEase AI</span>
        </Link>

        {/* Nav links */}
        <div className="hidden md:flex items-center gap-1">
          {links.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-150 flex items-center gap-1
                ${location.pathname === link.to
                  ? 'bg-primary-50 text-primary-700'
                  : 'text-gray-600 hover:bg-gray-50 hover:text-gray-800'}`}
              aria-label={link.label}
            >
              <span>{link.icon}</span>
              <span>{link.label}</span>
            </Link>
          ))}
        </div>

        {/* User + Logout */}
        <div className="flex items-center gap-3">
          {user && (
            <div className="hidden sm:flex items-center gap-2">
              <span className="text-xs text-gray-500 bg-gray-100 px-2 py-1 rounded-full">
                {user.role === 'CAREGIVER' ? '🤝 Caregiver' : '👤 Individual'}
              </span>
              <span className="text-sm font-medium text-gray-700">{user.name?.split(' ')[0]}</span>
            </div>
          )}
          <button
            onClick={handleLogout}
            className="text-sm text-gray-500 hover:text-red-500 transition-colors px-3 py-1 rounded-lg hover:bg-red-50"
            aria-label="Logout"
          >
            Logout
          </button>
        </div>
      </div>

      {/* Mobile bottom nav */}
      <div className="md:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 flex justify-around py-2 z-50">
        {links.map((link) => (
          <Link
            key={link.to}
            to={link.to}
            className={`flex flex-col items-center gap-0.5 px-3 py-1 rounded-lg text-xs
              ${location.pathname === link.to ? 'text-primary-600' : 'text-gray-400'}`}
            aria-label={link.label}
          >
            <span className="text-lg">{link.icon}</span>
            <span>{link.label}</span>
          </Link>
        ))}
      </div>
    </nav>
  );
}
