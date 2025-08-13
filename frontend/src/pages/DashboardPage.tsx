import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { getDashboardMetrics, getAllInvoices } from '../services/api';
import { DashboardMetrics, Invoice } from '../types';
import { useLanguage } from '../i18n';
import { Card, Button, StatusBadge, Table, ErrorState } from '../components/UI';
import { 
    PlusIcon, 
    DocumentTextIcon, 
    CalendarIcon,
    CurrencyEuroIcon,
    ChartBarIcon,
    ClockIcon
} from '@heroicons/react/24/outline';

interface DashboardPageProps { }

const DashboardPage: React.FC<DashboardPageProps> = () => {
    const { translations, language } = useLanguage();
    const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
    const [recentInvoices, setRecentInvoices] = useState<Invoice[]>([]);
    const [selectedPeriod, setSelectedPeriod] = useState<'month' | 'quarter' | 'year'>('month');
    const [selectedYear, setSelectedYear] = useState<number>(new Date().getFullYear());
    const [selectedMonth, setSelectedMonth] = useState<number>(new Date().getMonth() + 1);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchDashboardData = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);

            // Fetch metrics
            const metricsResponse = await getDashboardMetrics({
                period: selectedPeriod,
                year: selectedYear,
                month: selectedPeriod === 'month' ? selectedMonth : undefined,
            });
            setMetrics(metricsResponse);

            // Fetch recent invoices
            const invoicesResponse = await getAllInvoices();
            setRecentInvoices(invoicesResponse.slice(0, 5)); // Show last 5 invoices

        } catch (err) {
            setError(err instanceof Error ? err.message : 'Unknown error occurred');
        } finally {
            setLoading(false);
        }
    }, [selectedPeriod, selectedYear, selectedMonth]);

    useEffect(() => {
        fetchDashboardData();
    }, [fetchDashboardData]);

    const formatCurrency = (amount: number) => {
        const locale = language === 'de' ? 'de-DE' : 'en-US';
        return new Intl.NumberFormat(locale, {
            style: 'currency',
            currency: 'EUR',
        }).format(amount);
    };

    // Removed unused getStatusColor helper (StatusBadge handles styling)

    const getStatusText = (status: string) => {
        switch (status) {
            case 'paid':
                return translations.dashboard.status.paid;
            case 'sent':
                return translations.dashboard.status.sent;
            case 'created':
                return translations.dashboard.status.created;
            default:
                return status;
        }
    };

    const getCurrentMonthName = () => {
        return translations.dashboard.months[selectedMonth - 1];
    };

    const getPeriodDisplayName = () => {
        switch (selectedPeriod) {
            case 'month':
                return `${getCurrentMonthName()} ${selectedYear}`;
            case 'quarter':
                const quarter = Math.ceil(selectedMonth / 3);
                return `Q${quarter} ${selectedYear}`;
            case 'year':
                return `${selectedYear}`;
            default:
                return '';
        }
    };

    // Show header even while loading so tests that assert title presence immediately pass.
    // Only short-circuit fully on error (tests look for error specific messaging then retry button).
    if (error) {
        return (
            <ErrorState 
                message={`${translations.dashboard.errorLoading}: ${error}`}
                onRetry={fetchDashboardData}
                retryLabel={translations.common.buttons.tryAgain}
            />
        );
    }

    return (
        <div className="space-y-8">
            {/* Header */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">{translations.dashboard.title}</h1>
                    <p className="text-gray-600 mt-1">
                        {translations.dashboard.subtitle}
                    </p>
                </div>

                <div className="flex flex-wrap gap-3">
                    <select
                        value={selectedPeriod}
                        onChange={(e) => setSelectedPeriod(e.target.value as 'month' | 'quarter' | 'year')}
                        className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    >
                        <option value="month">{translations.dashboard.periods.month}</option>
                        <option value="quarter">{translations.dashboard.periods.quarter}</option>
                        <option value="year">{translations.dashboard.periods.year}</option>
                    </select>

                    <select
                        value={selectedYear}
                        onChange={(e) => setSelectedYear(parseInt(e.target.value))}
                        className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    >
                        {[2023, 2024, 2025, 2026].map(year => (
                            <option key={year} value={year}>{year}</option>
                        ))}
                    </select>

                    {selectedPeriod === 'month' && (
                        <select
                            value={selectedMonth}
                            onChange={(e) => setSelectedMonth(parseInt(e.target.value))}
                            className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        >
                            {Array.from({ length: 12 }, (_, i) => i + 1).map(month => (
                                <option key={month} value={month}>
                                    {translations.dashboard.months[month - 1]}
                                </option>
                            ))}
                        </select>
                    )}
                </div>
            </div>
            {loading && (
                <p className="text-sm text-gray-500" aria-label="loading-indicator">Loading...</p>
            )}

            {/* Metrics Cards (show skeleton when loading) */}
            {loading ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-6" data-testid="metrics-skeleton">
                    {Array.from({ length: 5 }).map((_, i) => (
                        <Card key={i} className="animate-pulse text-center">
                            <div className="w-12 h-12 rounded-lg bg-gray-200 mx-auto mb-4" />
                            <div className="h-4 bg-gray-200 rounded w-1/2 mx-auto mb-2" />
                            <div className="h-3 bg-gray-100 rounded w-2/3 mx-auto mb-1" />
                            <div className="h-6 bg-gray-300 rounded w-1/3 mx-auto" />
                        </Card>
                    ))}
                </div>
            ) : metrics && (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-6">
                    <Card className="text-center">
                        <div className="flex items-center justify-center w-12 h-12 bg-emerald-100 rounded-lg mx-auto mb-4">
                            <CurrencyEuroIcon className="w-6 h-6 text-emerald-600" />
                        </div>
                        <h3 className="text-sm font-medium text-gray-500 mb-2">
                            {translations.dashboard.metrics.revenue}
                        </h3>
                        <p className="text-sm text-gray-400 mb-1">({getPeriodDisplayName()})</p>
                        <p className="text-2xl font-bold text-emerald-600">
                            {formatCurrency(metrics.total_revenue_period)}
                        </p>
                    </Card>

                    <Card className="text-center">
                        <div className="flex items-center justify-center w-12 h-12 bg-amber-100 rounded-lg mx-auto mb-4">
                            <ClockIcon className="w-6 h-6 text-amber-600" />
                        </div>
                        <h3 className="text-sm font-medium text-gray-500 mb-2">
                            {translations.dashboard.metrics.pendingInvoices}
                        </h3>
                        <p className="text-2xl font-bold text-amber-600">
                            {formatCurrency(metrics.pending_invoices_amount)}
                        </p>
                    </Card>

                    <Card className="text-center">
                        <div className="flex items-center justify-center w-12 h-12 bg-blue-100 rounded-lg mx-auto mb-4">
                            <DocumentTextIcon className="w-6 h-6 text-blue-600" />
                        </div>
                        <h3 className="text-sm font-medium text-gray-500 mb-2">
                            {translations.dashboard.metrics.totalInvoices}
                        </h3>
                        <p className="text-2xl font-bold text-blue-600">
                            {metrics.total_invoices_count}
                        </p>
                    </Card>

                    <Card className="text-center">
                        <div className="flex items-center justify-center w-12 h-12 bg-emerald-100 rounded-lg mx-auto mb-4">
                            <ChartBarIcon className="w-6 h-6 text-emerald-600" />
                        </div>
                        <h3 className="text-sm font-medium text-gray-500 mb-2">
                            {translations.dashboard.metrics.paidInvoices}
                        </h3>
                        <p className="text-2xl font-bold text-emerald-600">
                            {metrics.paid_invoices_count}
                        </p>
                    </Card>

                    <Card className="text-center">
                        <div className="flex items-center justify-center w-12 h-12 bg-amber-100 rounded-lg mx-auto mb-4">
                            <DocumentTextIcon className="w-6 h-6 text-amber-600" />
                        </div>
                        <h3 className="text-sm font-medium text-gray-500 mb-2">
                            {translations.dashboard.metrics.sentInvoices}
                        </h3>
                        <p className="text-2xl font-bold text-amber-600">
                            {metrics.pending_invoices_count}
                        </p>
                    </Card>
                </div>
            )}

            {/* Recent Invoices */}
            <Card 
                title={translations.dashboard.recentInvoices.title}
                actions={
                    <Link to="/invoices">
                        <Button variant="secondary" size="sm">
                            {translations.common.buttons.viewAll}
                        </Button>
                    </Link>
                }
            >
                {loading ? (
                    <div className="animate-pulse space-y-2" data-testid="invoices-skeleton">
                        <div className="h-6 bg-gray-200 rounded w-full" />
                        {Array.from({ length: 5 }).map((_, i) => (
                            <div key={i} className="h-10 bg-gray-100 rounded" />
                        ))}
                    </div>
                ) : (
                    <Table
                        columns={[
                            { 
                                key: 'invoice_number', 
                                label: translations.dashboard.recentInvoices.columns.invoiceNumber,
                                render: (value) => <span className="font-mono text-sm">{value}</span>
                            },
                            { key: 'client_name', label: translations.dashboard.recentInvoices.columns.client },
                            { 
                                key: 'date', 
                                label: translations.dashboard.recentInvoices.columns.date,
                                render: (value) => new Date(value).toLocaleDateString(language === 'de' ? 'de-DE' : 'en-US')
                            },
                            { 
                                key: 'total_amount', 
                                label: translations.dashboard.recentInvoices.columns.amount,
                                render: (value) => <span className="font-semibold">{formatCurrency(value)}</span>
                            },
                            { 
                                key: 'status', 
                                label: translations.dashboard.recentInvoices.columns.status,
                                render: (value) => (
                                    <StatusBadge status={value}>
                                        {getStatusText(value)}
                                    </StatusBadge>
                                )
                            },
                        ]}
                        data={recentInvoices}
                        emptyMessage={translations.dashboard.recentInvoices.noInvoices}
                    />
                )}
            </Card>

            {/* Quick Actions */}
            <Card title={translations.dashboard.quickActions.title}>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                    <Link to="/sessions/new" className="block">
                        <div className="p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-all duration-200 text-center group">
                            <CalendarIcon className="w-8 h-8 text-gray-400 group-hover:text-blue-500 mx-auto mb-2" />
                            <h3 className="font-medium text-gray-900 group-hover:text-blue-700">
                                {translations.dashboard.quickActions.newSession}
                            </h3>
                            <p className="text-sm text-gray-500 mt-1">
                                {translations.dashboard.quickActionsDescriptions.newSession}
                            </p>
                        </div>
                    </Link>

                    <Link to="/invoices/generate" className="block">
                        <div className="p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-emerald-500 hover:bg-emerald-50 transition-all duration-200 text-center group">
                            <DocumentTextIcon className="w-8 h-8 text-gray-400 group-hover:text-emerald-500 mx-auto mb-2" />
                            <h3 className="font-medium text-gray-900 group-hover:text-emerald-700">
                                {translations.dashboard.quickActions.newInvoice}
                            </h3>
                            <p className="text-sm text-gray-500 mt-1">
                                {translations.dashboard.quickActionsDescriptions.newInvoice}
                            </p>
                        </div>
                    </Link>

                    <Link to="/clients/new" className="block">
                        <div className="p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-indigo-500 hover:bg-indigo-50 transition-all duration-200 text-center group">
                            <PlusIcon className="w-8 h-8 text-gray-400 group-hover:text-indigo-500 mx-auto mb-2" />
                            <h3 className="font-medium text-gray-900 group-hover:text-indigo-700">
                                {translations.clients.addNew}
                            </h3>
                            <p className="text-sm text-gray-500 mt-1">
                                {translations.dashboard.quickActionsDescriptions.addClient}
                            </p>
                        </div>
                    </Link>
                </div>
            </Card>
        </div>
    );
};

export default DashboardPage;
