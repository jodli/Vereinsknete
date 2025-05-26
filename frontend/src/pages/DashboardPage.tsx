import React, { useState, useEffect, useCallback } from 'react';
import { getDashboardMetrics, getAllInvoices } from '../services/api';
import { DashboardMetrics, Invoice } from '../types';
import { useLanguage } from '../i18n';

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

    const getStatusColor = (status: string) => {
        switch (status) {
            case 'paid':
                return 'text-green-600 bg-green-100';
            case 'sent':
                return 'text-yellow-600 bg-yellow-100';
            case 'created':
                return 'text-blue-600 bg-blue-100';
            default:
                return 'text-gray-600 bg-gray-100';
        }
    };

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

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-64">
                <div className="text-lg">{translations.dashboard.loading}</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                {translations.dashboard.errorLoading}: {error}
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-3xl font-bold text-gray-900">{translations.dashboard.title}</h1>

                <div className="flex gap-4">
                    <select
                        value={selectedPeriod}
                        onChange={(e) => setSelectedPeriod(e.target.value as 'month' | 'quarter' | 'year')}
                        className="border border-gray-300 rounded-md px-3 py-2"
                    >
                        <option value="month">{translations.dashboard.periods.month}</option>
                        <option value="quarter">{translations.dashboard.periods.quarter}</option>
                        <option value="year">{translations.dashboard.periods.year}</option>
                    </select>

                    <select
                        value={selectedYear}
                        onChange={(e) => setSelectedYear(parseInt(e.target.value))}
                        className="border border-gray-300 rounded-md px-3 py-2"
                    >
                        {[2023, 2024, 2025, 2026].map(year => (
                            <option key={year} value={year}>{year}</option>
                        ))}
                    </select>

                    {selectedPeriod === 'month' && (
                        <select
                            value={selectedMonth}
                            onChange={(e) => setSelectedMonth(parseInt(e.target.value))}
                            className="border border-gray-300 rounded-md px-3 py-2"
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

            {/* Metrics Cards */}
            {metrics && (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
                    <div className="bg-white p-6 rounded-lg shadow">
                        <h3 className="text-sm font-medium text-gray-500">{translations.dashboard.metrics.revenue} ({getPeriodDisplayName()})</h3>
                        <p className="text-2xl font-bold text-green-600">{formatCurrency(metrics.total_revenue_period)}</p>
                    </div>

                    <div className="bg-white p-6 rounded-lg shadow">
                        <h3 className="text-sm font-medium text-gray-500">{translations.dashboard.metrics.pendingInvoices}</h3>
                        <p className="text-2xl font-bold text-yellow-600">{formatCurrency(metrics.pending_invoices_amount)}</p>
                    </div>

                    <div className="bg-white p-6 rounded-lg shadow">
                        <h3 className="text-sm font-medium text-gray-500">{translations.dashboard.metrics.totalInvoices}</h3>
                        <p className="text-2xl font-bold text-blue-600">{metrics.total_invoices_count}</p>
                    </div>

                    <div className="bg-white p-6 rounded-lg shadow">
                        <h3 className="text-sm font-medium text-gray-500">{translations.dashboard.metrics.paidInvoices}</h3>
                        <p className="text-2xl font-bold text-green-600">{metrics.paid_invoices_count}</p>
                    </div>

                    <div className="bg-white p-6 rounded-lg shadow">
                        <h3 className="text-sm font-medium text-gray-500">{translations.dashboard.metrics.sentInvoices}</h3>
                        <p className="text-2xl font-bold text-yellow-600">{metrics.pending_invoices_count}</p>
                    </div>
                </div>
            )}

            {/* Recent Invoices */}
            <div className="bg-white rounded-lg shadow">
                <div className="px-6 py-4 border-b border-gray-200">
                    <h2 className="text-xl font-semibold text-gray-900">{translations.dashboard.recentInvoices.title}</h2>
                </div>
                <div className="p-6">
                    {recentInvoices.length === 0 ? (
                        <p className="text-gray-500">{translations.dashboard.recentInvoices.noInvoices}</p>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="min-w-full">
                                <thead>
                                    <tr className="border-b border-gray-200">
                                        <th className="text-left py-2 font-medium text-gray-700">{translations.dashboard.recentInvoices.columns.invoiceNumber}</th>
                                        <th className="text-left py-2 font-medium text-gray-700">{translations.dashboard.recentInvoices.columns.client}</th>
                                        <th className="text-left py-2 font-medium text-gray-700">{translations.dashboard.recentInvoices.columns.date}</th>
                                        <th className="text-left py-2 font-medium text-gray-700">{translations.dashboard.recentInvoices.columns.amount}</th>
                                        <th className="text-left py-2 font-medium text-gray-700">{translations.dashboard.recentInvoices.columns.status}</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {recentInvoices.map((invoice) => (
                                        <tr key={invoice.id} className="border-b border-gray-100">
                                            <td className="py-3 font-mono text-sm">{invoice.invoice_number}</td>
                                            <td className="py-3">{invoice.client_name}</td>
                                            <td className="py-3">{new Date(invoice.date).toLocaleDateString(language === 'de' ? 'de-DE' : 'en-US')}</td>
                                            <td className="py-3 font-semibold">{formatCurrency(invoice.total_amount)}</td>
                                            <td className="py-3">
                                                <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(invoice.status)}`}>
                                                    {getStatusText(invoice.status)}
                                                </span>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>

            {/* Quick Actions */}
            <div className="bg-white rounded-lg shadow p-6">
                <h2 className="text-xl font-semibold text-gray-900 mb-4">{translations.dashboard.quickActions.title}</h2>
                <div className="flex gap-4">
                    <button
                        onClick={() => window.location.href = '/sessions/new'}
                        className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors"
                    >
                        {translations.dashboard.quickActions.newSession}
                    </button>
                    <button
                        onClick={() => window.location.href = '/invoice'}
                        className="bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 transition-colors"
                    >
                        {translations.dashboard.quickActions.newInvoice}
                    </button>
                    <button
                        onClick={() => window.location.href = '/invoices'}
                        className="bg-gray-600 text-white px-4 py-2 rounded-md hover:bg-gray-700 transition-colors"
                    >
                        {translations.dashboard.quickActions.allInvoices}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default DashboardPage;
