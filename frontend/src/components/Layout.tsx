import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
    HomeIcon,
    UserIcon,
    UsersIcon,
    CalendarIcon,
    DocumentTextIcon,
    Bars3Icon,
    XMarkIcon,
    ChevronLeftIcon,
    Cog6ToothIcon
} from '@heroicons/react/24/outline';
import { useLanguage } from '../i18n';

interface LayoutProps {
    children: React.ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
    const location = useLocation();
    const { translations } = useLanguage();
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

    const navigation = [
        { name: translations.navigation.dashboard, href: '/', icon: HomeIcon, color: 'text-blue-600' },
        { name: translations.navigation.clients, href: '/clients', icon: UsersIcon, color: 'text-green-600' },
        { name: translations.navigation.sessions, href: '/sessions', icon: CalendarIcon, color: 'text-purple-600' },
        { name: translations.navigation.invoices, href: '/invoices', icon: DocumentTextIcon, color: 'text-orange-600' },
    ];

    const secondaryNavigation = [
        { name: translations.navigation.myDetails, href: '/profile', icon: UserIcon, color: 'text-gray-600' },
        { name: translations.navigation.settings, href: '/settings', icon: Cog6ToothIcon, color: 'text-gray-600' },
    ];

    const isActive = (path: string) => {
        if (path === '/') {
            return location.pathname === '/';
        }
        return location.pathname.startsWith(path);
    };

    const closeSidebar = () => setSidebarOpen(false);

    return (
        <div className="flex h-screen bg-gray-50">
            {/* Mobile sidebar overlay */}
            {sidebarOpen && (
                <div
                    className="fixed inset-0 z-40 bg-black bg-opacity-50 lg:hidden"
                    onClick={closeSidebar}
                />
            )}

            {/* Sidebar */}
            <div className={`
                ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}
                lg:translate-x-0
                ${sidebarCollapsed ? 'lg:w-16' : 'lg:w-64'}
                fixed lg:relative inset-y-0 left-0 z-50 w-64
                bg-white shadow-xl border-r border-gray-200
                transition-all duration-300 ease-in-out
                flex flex-col
            `}>
                {/* Sidebar Header */}
                <div className="flex items-center justify-between p-4 border-b border-gray-200">
                    {!sidebarCollapsed && (
                        <div className="flex items-center space-x-3">
                            <div className="w-8 h-8 bg-gradient-to-r from-blue-600 to-purple-600 rounded-lg flex items-center justify-center">
                                <span className="text-white font-bold text-sm">VK</span>
                            </div>
                            <h1 className="text-xl font-bold text-gray-900">VereinsKnete</h1>
                        </div>
                    )}

                    <div className="flex items-center space-x-2">
                        {/* {!sidebarCollapsed && (
                            <div className="lg:hidden">
                                <LanguageSwitcher />
                            </div>
                        )} */}

                        <button
                            onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
                            className="hidden lg:flex p-1.5 rounded-md text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
                        >
                            <ChevronLeftIcon className={`w-4 h-4 transition-transform ${sidebarCollapsed ? 'rotate-180' : ''}`} />
                        </button>

                        {/* Mobile close button: Only show if sidebar is open */}
                        {sidebarOpen && (
                            <button
                                onClick={closeSidebar}
                                className="lg:hidden p-1.5 rounded-md text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
                            >
                                <XMarkIcon className="w-5 h-5" />
                            </button>
                        )}
                    </div>
                </div>

                {/* Language Switcher for collapsed sidebar */}
                {/* {sidebarCollapsed && (
                    <div className="px-2 py-2 border-b border-gray-200">
                        <LanguageSwitcher />
                    </div>
                )} */}

                {/* Main Navigation */}
                <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
                    <div className={`${sidebarCollapsed ? 'hidden' : 'block'} mb-4`}>
                        <p className="px-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                            Main
                        </p>
                    </div>

                    {navigation.map((item) => (
                        <Link
                            key={item.name}
                            to={item.href}
                            onClick={closeSidebar}
                            className={`group flex items-center px-3 py-2.5 text-sm font-medium rounded-lg transition-all duration-200 ${isActive(item.href)
                                ? 'bg-blue-50 text-blue-700 border-r-2 border-blue-600'
                                : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                                }`}
                            title={sidebarCollapsed ? item.name : undefined}
                        >
                            <item.icon
                                className={`${sidebarCollapsed ? 'mx-auto' : 'mr-3'} w-5 h-5 transition-colors ${isActive(item.href) ? item.color : 'text-gray-500 group-hover:text-gray-700'
                                    }`}
                            />
                            {!sidebarCollapsed && (
                                <span className="truncate">{item.name}</span>
                            )}
                        </Link>
                    ))}

                    {/* Secondary Navigation */}
                    <div className={`${sidebarCollapsed ? 'hidden' : 'block'} mt-8 mb-4`}>
                        <p className="px-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                            Account
                        </p>
                    </div>

                    {secondaryNavigation.map((item) => (
                        <Link
                            key={item.name}
                            to={item.href}
                            onClick={closeSidebar}
                            className={`group flex items-center px-3 py-2.5 text-sm font-medium rounded-lg transition-all duration-200 ${isActive(item.href)
                                ? 'bg-gray-100 text-gray-900'
                                : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                                }`}
                            title={sidebarCollapsed ? item.name : undefined}
                        >
                            <item.icon
                                className={`${sidebarCollapsed ? 'mx-auto' : 'mr-3'} w-5 h-5 text-gray-500 group-hover:text-gray-700`}
                            />
                            {!sidebarCollapsed && (
                                <span className="truncate">{item.name}</span>
                            )}
                        </Link>
                    ))}
                </nav>

                {/* User section (prepared for future auth) */}
                {/* <div className="border-t border-gray-200 p-3">
                    {!sidebarCollapsed && (
                        <div className="flex items-center space-x-3 p-2">
                            <div className="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center">
                                <UserIcon className="w-4 h-4 text-gray-600" />
                            </div>
                            <div className="flex-1 min-w-0">
                                <p className="text-sm font-medium text-gray-900 truncate">
                                    User Name
                                </p>
                                <p className="text-xs text-gray-500 truncate">
                                    user@example.com
                                </p>
                            </div>
                            <button className="p-1 text-gray-400 hover:text-gray-600 transition-colors">
                                <ArrowRightOnRectangleIcon className="w-4 h-4" />
                            </button>
                        </div>
                    )}

                    {!sidebarCollapsed && (
                        <div className="mt-2">
                            <LanguageSwitcher />
                        </div>
                    )}
                </div> */}
            </div>

            {/* Main Content */}
            <div className="flex-1 flex flex-col overflow-hidden">
                {/* Mobile header */}
                <div className="lg:hidden bg-white border-b border-gray-200 px-4 py-3 flex items-center justify-between">
                    <button
                        onClick={() => setSidebarOpen(true)}
                        className="p-2 rounded-md text-gray-400 hover:text-gray-600 hover:bg-gray-100"
                    >
                        <Bars3Icon className="w-6 h-6" />
                    </button>

                    <div className="flex items-center space-x-2">
                        <div className="w-6 h-6 bg-gradient-to-r from-blue-600 to-purple-600 rounded"></div>
                        <h1 className="text-lg font-semibold text-gray-900">VereinsKnete</h1>
                    </div>

                    <div className="w-10"></div> {/* Spacer for centering */}
                </div>

                {/* Main content area */}
                <main className="flex-1 overflow-auto bg-gray-50">
                    <div className="p-6">
                        {children}
                    </div>
                </main>
            </div>
        </div>
    );
};

export default Layout;
