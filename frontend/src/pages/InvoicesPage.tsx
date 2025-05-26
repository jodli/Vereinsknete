import React, { useState, useEffect } from 'react';
import { Card, Select, Button } from '../components/UI';
import {
    getAllInvoices,
    updateInvoiceStatus,
    getClients,
    generateInvoice,
    downloadInvoicePdf,
    deleteInvoice
} from '../services/api';
import { Invoice, Client } from '../types';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import { useLanguage } from '../i18n';
import {
    DocumentArrowDownIcon,
    PlusIcon,
    CheckCircleIcon,
    ClockIcon,
    PaperAirplaneIcon,
    XMarkIcon,
    TrashIcon
} from '@heroicons/react/24/outline';

const InvoicesPage: React.FC = () => {
    const [invoices, setInvoices] = useState<Invoice[]>([]);
    const [clients, setClients] = useState<Client[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isGenerating, setIsGenerating] = useState(false);
    const [isUpdatingStatus, setIsUpdatingStatus] = useState<number | null>(null);
    const [isDownloading, setIsDownloading] = useState<number | null>(null);
    const [isDeleting, setIsDeleting] = useState<number | null>(null);
    const [error, setError] = useState('');
    const [showGenerateForm, setShowGenerateForm] = useState(false);
    const { language } = useLanguage();

    const [formData, setFormData] = useState({
        client_id: '',
        start_date: new Date(new Date().getFullYear(), new Date().getMonth(), 1),
        end_date: new Date(),
    });

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            setIsLoading(true);
            const [invoicesData, clientsData] = await Promise.all([
                getAllInvoices(),
                getClients()
            ]);

            setInvoices(invoicesData);
            setClients(clientsData);

            if (clientsData.length > 0) {
                setFormData(prev => ({ ...prev, client_id: clientsData[0].id.toString() }));
            }
            setError('');
        } catch (error) {
            console.error('Error fetching data:', error);
            setError('Failed to load data');
        } finally {
            setIsLoading(false);
        }
    };

    const handleStatusUpdate = async (invoiceId: number, newStatus: 'created' | 'sent' | 'paid') => {
        try {
            setIsUpdatingStatus(invoiceId);
            await updateInvoiceStatus(invoiceId, { status: newStatus });

            // Update local state
            setInvoices(prevInvoices =>
                prevInvoices.map(invoice =>
                    invoice.id === invoiceId
                        ? { ...invoice, status: newStatus }
                        : invoice
                )
            );
        } catch (error) {
            console.error('Error updating invoice status:', error);
            setError('Failed to update invoice status');
        } finally {
            setIsUpdatingStatus(null);
        }
    };

    const handleDownloadPdf = async (invoiceId: number, invoiceNumber: string) => {
        try {
            setIsDownloading(invoiceId);
            const pdfBlob = await downloadInvoicePdf(invoiceId);

            // Create download link
            const url = window.URL.createObjectURL(pdfBlob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `invoice_${invoiceNumber}.pdf`;
            document.body.appendChild(a);
            a.click();

            // Cleanup
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } catch (error) {
            console.error('Error downloading PDF:', error);
            setError('Failed to download PDF');
        } finally {
            setIsDownloading(null);
        }
    };

    const handleDeleteInvoice = async (invoiceId: number, invoiceNumber: string) => {
        if (!window.confirm(`Sind Sie sicher, dass Sie die Rechnung ${invoiceNumber} löschen möchten? Diese Aktion kann nicht rückgängig gemacht werden.`)) {
            return;
        }

        try {
            setIsDeleting(invoiceId);
            await deleteInvoice(invoiceId);

            // Remove from local state
            setInvoices(prevInvoices =>
                prevInvoices.filter(invoice => invoice.id !== invoiceId)
            );

            setError('');
        } catch (error) {
            console.error('Error deleting invoice:', error);
            setError('Failed to delete invoice');
        } finally {
            setIsDeleting(null);
        }
    };

    const handleGenerateInvoice = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsGenerating(true);
        setError('');

        try {
            const formattedStartDate = formData.start_date.toISOString().split('T')[0];
            const formattedEndDate = formData.end_date.toISOString().split('T')[0];

            const response = await generateInvoice({
                client_id: parseInt(formData.client_id),
                start_date: formattedStartDate,
                end_date: formattedEndDate,
                language: language,
            });

            // Convert base64 to blob and trigger download
            const pdfBytes = Uint8Array.from(atob(response.pdf_bytes), c => c.charCodeAt(0));
            const pdfBlob = new Blob([pdfBytes], { type: 'application/pdf' });
            const url = window.URL.createObjectURL(pdfBlob);
            const a = document.createElement('a');

            a.href = url;
            a.download = `invoice_${response.invoice_number}.pdf`;
            document.body.appendChild(a);
            a.click();

            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);

            // Refresh invoices list
            await fetchData();
            setShowGenerateForm(false);

        } catch (error) {
            console.error('Error generating invoice:', error);
            setError('Failed to generate invoice');
        } finally {
            setIsGenerating(false);
        }
    };

    const getStatusIcon = (status: string) => {
        switch (status) {
            case 'created':
                return <ClockIcon className="w-5 h-5 text-gray-500" />;
            case 'sent':
                return <PaperAirplaneIcon className="w-5 h-5 text-blue-500" />;
            case 'paid':
                return <CheckCircleIcon className="w-5 h-5 text-green-500" />;
            default:
                return <ClockIcon className="w-5 h-5 text-gray-500" />;
        }
    };

    const getStatusText = (status: string) => {
        switch (status) {
            case 'created':
                return 'Erstellt';
            case 'sent':
                return 'Versendet';
            case 'paid':
                return 'Bezahlt';
            default:
                return 'Unbekannt';
        }
    };

    const formatCurrency = (amount: number) => {
        return new Intl.NumberFormat('de-DE', {
            style: 'currency',
            currency: 'EUR',
        }).format(amount);
    };

    const formatDate = (dateStr: string) => {
        return new Date(dateStr).toLocaleDateString('de-DE');
    };

    if (isLoading) {
        return (
            <div className="flex justify-center items-center h-64">
                <p className="text-gray-600">Loading...</p>
            </div>
        );
    }

    return (
        <div>
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Rechnungen</h1>
                <Button
                    onClick={() => setShowGenerateForm(!showGenerateForm)}
                    className="flex items-center gap-2"
                >
                    {showGenerateForm ? (
                        <>
                            <XMarkIcon className="w-5 h-5" />
                            Abbrechen
                        </>
                    ) : (
                        <>
                            <PlusIcon className="w-5 h-5" />
                            Neue Rechnung
                        </>
                    )}
                </Button>
            </div>

            {error && (
                <div className="mb-4 p-3 bg-red-100 text-red-800 rounded-lg">
                    {error}
                </div>
            )}

            {/* Invoice Generation Form */}
            {showGenerateForm && (
                <Card className="mb-6">
                    <h2 className="text-lg font-semibold mb-4">Neue Rechnung erstellen</h2>

                    {clients.length === 0 ? (
                        <div className="text-center py-6">
                            <p className="text-gray-700 mb-4">Keine Klienten vorhanden. Fügen Sie zuerst einen Klienten hinzu.</p>
                            <Button onClick={() => window.location.href = '/clients/new'}>
                                Klient hinzufügen
                            </Button>
                        </div>
                    ) : (
                        <form onSubmit={handleGenerateInvoice}>
                            <p className="text-sm text-gray-600 mb-6">
                                Wählen Sie einen Klienten und einen Zeitraum, um eine Rechnung für alle Sitzungen in diesem Zeitraum zu erstellen.
                            </p>

                            <Select
                                id="client_id"
                                label="Klient"
                                value={formData.client_id}
                                onChange={(e) => setFormData(prev => ({ ...prev, client_id: e.target.value }))}
                                options={clients.map(client => ({
                                    value: client.id,
                                    label: client.name
                                }))}
                                required
                            />

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Startdatum<span className="text-red-500">*</span>
                                    </label>
                                    <DatePicker
                                        selected={formData.start_date}
                                        onChange={(date) => date && setFormData(prev => ({ ...prev, start_date: date }))}
                                        className="w-full p-2 border border-gray-300 rounded-md"
                                        dateFormat="dd.MM.yyyy"
                                        required
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Enddatum<span className="text-red-500">*</span>
                                    </label>
                                    <DatePicker
                                        selected={formData.end_date}
                                        onChange={(date) => date && setFormData(prev => ({ ...prev, end_date: date }))}
                                        className="w-full p-2 border border-gray-300 rounded-md"
                                        dateFormat="dd.MM.yyyy"
                                        minDate={formData.start_date}
                                        required
                                    />
                                </div>
                            </div>

                            <div className="flex justify-end mt-6">
                                <Button type="submit" disabled={isGenerating}>
                                    {isGenerating ? 'Wird erstellt...' : 'Rechnung erstellen'}
                                </Button>
                            </div>
                        </form>
                    )}
                </Card>
            )}

            {/* Invoices List */}
            <Card>
                <h2 className="text-lg font-semibold mb-4">Alle Rechnungen</h2>

                {invoices.length === 0 ? (
                    <div className="text-center py-8">
                        <p className="text-gray-600">Noch keine Rechnungen vorhanden.</p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead>
                                <tr className="border-b">
                                    <th className="text-left py-3 px-4">Rechnungsnummer</th>
                                    <th className="text-left py-3 px-4">Klient</th>
                                    <th className="text-left py-3 px-4">Datum</th>
                                    <th className="text-left py-3 px-4">Betrag</th>
                                    <th className="text-left py-3 px-4">Status</th>
                                    <th className="text-left py-3 px-4">Aktionen</th>
                                </tr>
                            </thead>
                            <tbody>
                                {invoices.map((invoice) => (
                                    <tr key={invoice.id} className="border-b hover:bg-gray-50">
                                        <td className="py-3 px-4 font-mono">
                                            {invoice.invoice_number}
                                        </td>
                                        <td className="py-3 px-4">{invoice.client_name}</td>
                                        <td className="py-3 px-4">{formatDate(invoice.created_at)}</td>
                                        <td className="py-3 px-4 font-semibold">{formatCurrency(invoice.total_amount)}</td>
                                        <td className="py-3 px-4">
                                            <div className="flex items-center gap-2">
                                                {getStatusIcon(invoice.status)}
                                                <span className={`px-2 py-1 rounded-full text-xs font-medium ${invoice.status === 'paid' ? 'bg-green-100 text-green-800' :
                                                    invoice.status === 'sent' ? 'bg-blue-100 text-blue-800' :
                                                        'bg-gray-100 text-gray-800'
                                                    }`}>
                                                    {getStatusText(invoice.status)}
                                                </span>
                                            </div>
                                        </td>
                                        <td className="py-3 px-4">
                                            <div className="flex items-center gap-2">
                                                <button
                                                    onClick={() => handleDownloadPdf(invoice.id, invoice.invoice_number)}
                                                    disabled={isDownloading === invoice.id}
                                                    className="p-1 text-gray-600 hover:text-blue-600 disabled:opacity-50"
                                                    title="PDF herunterladen"
                                                >
                                                    <DocumentArrowDownIcon className="w-5 h-5" />
                                                </button>

                                                {invoice.status === 'created' && (
                                                    <button
                                                        onClick={() => handleStatusUpdate(invoice.id, 'sent')}
                                                        disabled={isUpdatingStatus === invoice.id}
                                                        className="px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded hover:bg-blue-200 disabled:opacity-50"
                                                    >
                                                        Als versendet markieren
                                                    </button>
                                                )}

                                                {invoice.status === 'sent' && (
                                                    <button
                                                        onClick={() => handleStatusUpdate(invoice.id, 'paid')}
                                                        disabled={isUpdatingStatus === invoice.id}
                                                        className="px-2 py-1 text-xs bg-green-100 text-green-800 rounded hover:bg-green-200 disabled:opacity-50"
                                                    >
                                                        Als bezahlt markieren
                                                    </button>
                                                )}

                                                <button
                                                    onClick={() => handleDeleteInvoice(invoice.id, invoice.invoice_number)}
                                                    disabled={isDeleting === invoice.id}
                                                    className="p-1 text-gray-600 hover:text-red-600 disabled:opacity-50"
                                                    title="Rechnung löschen"
                                                >
                                                    <TrashIcon className="w-5 h-5" />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </Card>
        </div>
    );
};

export default InvoicesPage;
