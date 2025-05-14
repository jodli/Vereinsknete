import React, { useState, useEffect } from 'react';
import { Card, Select, Button } from '../components/UI';
import { getClients, generateInvoice } from '../services/api';
import { Client } from '../types';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";

const InvoicePage: React.FC = () => {
  const [clients, setClients] = useState<Client[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isGenerating, setIsGenerating] = useState(false);
  const [error, setError] = useState('');

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
        setError('Failed to load clients. Please try again later.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchClients();
  }, []);

  const handleClientChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setFormData(prev => ({ ...prev, client_id: e.target.value }));
  };

  const handleStartDateChange = (date: Date) => {
    setFormData(prev => ({ ...prev, start_date: date }));
  };

  const handleEndDateChange = (date: Date) => {
    setFormData(prev => ({ ...prev, end_date: date }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsGenerating(true);
    setError('');

    try {
      const formattedStartDate = formData.start_date.toISOString().split('T')[0];
      const formattedEndDate = formData.end_date.toISOString().split('T')[0];

      const pdfBlob = await generateInvoice({
        client_id: parseInt(formData.client_id),
        start_date: formattedStartDate,
        end_date: formattedEndDate,
      });

      // Create a URL for the blob and trigger download
      const url = window.URL.createObjectURL(pdfBlob);
      const a = document.createElement('a');

      // Format date for filename, e.g., "invoice_2023-05-14.pdf"
      const dateString = new Date().toISOString().split('T')[0];

      a.href = url;
      a.download = `invoice_${dateString}.pdf`;
      document.body.appendChild(a);
      a.click();

      // Cleanup
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);

      setIsGenerating(false);
    } catch (error) {
      console.error('Error generating invoice:', error);
      setError('Failed to generate invoice. Please try again.');
      setIsGenerating(false);
    }
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
      <h1 className="text-2xl font-bold mb-6">Generate Invoice</h1>

      <Card>
        {error && (
          <div className="mb-4 p-2 bg-red-100 text-red-800 rounded">
            {error}
          </div>
        )}

        {clients.length === 0 ? (
          <div className="text-center py-6">
            <p className="text-gray-700 mb-4">You need to add clients and log sessions before you can generate an invoice.</p>
            <Button onClick={() => window.location.href = '/clients/new'}>
              Add Your First Client
            </Button>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <p className="text-sm text-gray-600 mb-6">
              Select a client and date range to generate an invoice for all sessions within that period.
            </p>

            <Select
              id="client_id"
              label="Client"
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
                  Start Date<span className="text-red-500">*</span>
                </label>
                <DatePicker
                  selected={formData.start_date}
                  onChange={handleStartDateChange}
                  className="w-full p-2 border border-gray-300 rounded-md"
                  dateFormat="yyyy-MM-dd"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  End Date<span className="text-red-500">*</span>
                </label>
                <DatePicker
                  selected={formData.end_date}
                  onChange={handleEndDateChange}
                  className="w-full p-2 border border-gray-300 rounded-md"
                  dateFormat="yyyy-MM-dd"
                  minDate={formData.start_date}
                  required
                />
              </div>
            </div>

            <div className="flex justify-end mt-6">
              <Button type="submit" disabled={isGenerating}>
                {isGenerating ? 'Generating...' : 'Generate Invoice'}
              </Button>
            </div>
          </form>
        )}
      </Card>
    </div>
  );
};

export default InvoicePage;
