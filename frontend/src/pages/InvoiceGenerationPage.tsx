import React, { useState, useEffect } from 'react';
import { Card, Select, Button } from '../components/UI';
import { getClients, generateInvoice } from '../services/api';
import { Client } from '../types';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import { useLanguage } from '../i18n';

const InvoicePage: React.FC = () => {
    const [clients, setClients] = useState<Client[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isGenerating, setIsGenerating] = useState(false);
    const [error, setError] = useState('');
    const { translations, language } = useLanguage();

    const [formData, setFormData] = useState({
        client_id: '',
        start_date: new Date(new Date().getFullYear(), new Date().getMonth(), 1), // First day of current month
        end_date: new Date(), // Current date
    });

    useEffect(() => {
        const fetchClients = async () => {
            try {
                const data = await getClients();
                setClients(data);
                if (data.length > 0) {
                    setFormData(prev => ({ ...prev, client_id: data[0].id.toString() }));
                }
                setError('');
            } catch (error) {
                console.error('Error fetching clients:', error);
                setError(translations.common.errors.failedToLoad);
            } finally {
                setIsLoading(false);
            }
        };

        fetchClients();
    }, []);

    const handleClientChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setFormData(prev => ({ ...prev, client_id: e.target.value }));
    };

    const handleStartDateChange = (date: Date | null) => {
        if (date) {
            setFormData(prev => ({ ...prev, start_date: date }));
        }
    };

    const handleEndDateChange = (date: Date | null) => {
        if (date) {
            setFormData(prev => ({ ...prev, end_date: date }));
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
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

            // Cleanup
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);

            setIsGenerating(false);
        } catch (error) {
            console.error('Error generating invoice:', error);
            setError(translations.common.errors.failedToGenerate);
            setIsGenerating(false);
        }
    };

    if (isLoading) {
        return (
            <div className="flex justify-center items-center h-64">
                <p className="text-gray-600">{translations.common.loading}</p>
            </div>
        );
    }

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">{translations.invoices.generateNew}</h1>

            <Card>
                {error && (
                    <div className="mb-4 p-2 bg-red-100 text-red-800 rounded">
                        {error}
                    </div>
                )}

                {clients.length === 0 ? (
                    <div className="text-center py-6">
                        <p className="text-gray-700 mb-4">{translations.invoices.noClients}</p>
                        <Button onClick={() => window.location.href = '/clients/new'}>
                            {translations.dashboard.setupSteps.addClient.title}
                        </Button>
                    </div>
                ) : (
                    <form onSubmit={handleSubmit}>
                        <p className="text-sm text-gray-600 mb-6">
                            Wählen Sie einen Klienten und einen Zeitraum, um eine Rechnung für alle Sitzungen in diesem Zeitraum zu erstellen.
                        </p>

                        <Select
                            id="client_id"
                            label={translations.sessions.form.labels.client}
                            value={formData.client_id}
                            onChange={handleClientChange}
                            options={clients.map(client => ({
                                value: client.id,
                                label: client.name
                            }))}
                            required
                        />

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    {translations.sessions.columns.date} (von)<span className="text-red-500">*</span>
                                </label>
                                <DatePicker
                                    selected={formData.start_date}
                                    onChange={handleStartDateChange}
                                    className="w-full p-2 border border-gray-300 rounded-md"
                                    dateFormat="dd.MM.yyyy"
                                    required
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    {translations.sessions.columns.date} (bis)<span className="text-red-500">*</span>
                                </label>
                                <DatePicker
                                    selected={formData.end_date}
                                    onChange={handleEndDateChange}
                                    className="w-full p-2 border border-gray-300 rounded-md"
                                    dateFormat="dd.MM.yyyy"
                                    minDate={formData.start_date}
                                    required
                                />
                            </div>
                        </div>

                        <div className="flex justify-end mt-6">
                            <Button type="submit" disabled={isGenerating}>
                                {isGenerating ? `${translations.common.loading}...` : translations.invoices.generateNew}
                            </Button>
                        </div>
                    </form>
                )}
            </Card>
        </div>
    );
};

export default InvoicePage;
