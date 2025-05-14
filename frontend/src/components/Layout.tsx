import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
    HomeIcon,
    UserIcon,
    UsersIcon,
    CalendarIcon,
    DocumentTextIcon
} from '@heroicons/react/24/outline';

interface LayoutProps {
    children: React.ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
    const location = useLocation();

    const navigation = [
        { name: 'Dashboard', href: '/', icon: HomeIcon },
        { name: 'My Details', href: '/profile', icon: UserIcon },
        { name: 'Clients', href: '/clients', icon: UsersIcon },
        { name: 'Sessions', href: '/sessions', icon: CalendarIcon },
        { name: 'Invoices', href: '/invoices', icon: DocumentTextIcon },
    ];

    const isActive = (path: string) => {
        return location.pathname === path;
    };

    return (
        <div className="flex h-screen bg-gray-100">
            {/* Sidebar */}
            <div className="w-64 bg-white shadow-md">
                <div className="p-6">
                    <h1 className="text-2xl font-bold text-blue-600">VereinsKnete</h1>
                </div>
                <nav className="mt-6">
                    <ul>
                        {navigation.map((item) => (
                            <li key={item.name} className="px-2">
                                <Link
                                    to={item.href}
                                    className={`flex items-center px-4 py-3 mb-1 rounded-lg transition-colors ${isActive(item.href)
                                            ? 'bg-blue-100 text-blue-700 font-medium'
                                            : 'text-gray-600 hover:bg-gray-100'
                                        }`}
                                >
                                    <item.icon className="w-5 h-5 mr-3" aria-hidden="true" />
                                    {item.name}
                                </Link>
                            </li>
                        ))}
                    </ul>
                </nav>
            </div>

            {/* Main Content */}
            <div className="flex-1 overflow-auto">
                <main className="p-6">
                    {children}
                </main>
            </div>
        </div>
    );
};

export default Layout;
