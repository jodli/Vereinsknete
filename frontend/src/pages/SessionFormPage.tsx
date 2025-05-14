import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Input, Select, Button } from '../components/UI';
import { getClients, createSession } from '../services/api';
import { Client } from '../types';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";

const SessionFormPage: React.FC = () => {
    const navigate = useNavigate();

    const [clients, setClients] = useState<Client[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState('');

    const [formData, setFormData] = useState({
        client_id: '',
        name: '',
        date: new Date(),
        start_time: '',
        end_time: '',
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

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleDateChange = (date: Date | null) => {
        if (date) {
            setFormData(prev => ({ ...prev, date }));
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSaving(true);
        setError('');

        try {
            // Format the data for API submission
            const formattedDate = formData.date.toISOString().split('T')[0];

            await createSession({
                client_id: parseInt(formData.client_id),
                name: formData.name,
                date: formattedDate,
                start_time: formData.start_time,
                end_time: formData.end_time,
            });

            navigate('/sessions');
        } catch (error) {
            console.error('Error creating session:', error);
            setError('Failed to save session. Please try again.');
            setIsSaving(false);
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
            <h1 className="text-2xl font-bold mb-6">Log New Session</h1>

            <Card>
                {error && (
                    <div className="mb-4 p-2 bg-red-100 text-red-800 rounded">
                        {error}
                    </div>
                )}

                {clients.length === 0 ? (
                    <div className="text-center py-6">
                        <p className="text-gray-700 mb-4">You need to create a client first before you can log a session.</p>
                        <Button onClick={() => navigate('/clients/new')}>
                            Create Your First Client
                        </Button>
                    </div>
                ) : (
                    <form onSubmit={handleSubmit}>
                        <Select
                            id="client_id"
                            name="client_id"
                            label="Client"
                            value={formData.client_id}
                            onChange={handleChange}
                            options={clients.map(client => ({
                                value: client.id,
                                label: client.name
                            }))}
                            required
                        />

                        <Input
                            id="name"
                            name="name"
                            label="Session Name"
                            value={formData.name}
                            onChange={handleChange}
                            placeholder="e.g. Yoga Class, Training Session"
                            required
                        />

                        <div className="mb-4">
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Date<span className="text-red-500">*</span>
                            </label>
                            <DatePicker
                                selected={formData.date}
                                onChange={handleDateChange}
                                className="w-full p-2 border border-gray-300 rounded-md"
                                dateFormat="yyyy-MM-dd"
                                required
                            />
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <Input
                                id="start_time"
                                name="start_time"
                                label="Start Time"
                                type="time"
                                value={formData.start_time}
                                onChange={handleChange}
                                required
                            />

                            <Input
                                id="end_time"
                                name="end_time"
                                label="End Time"
                                type="time"
                                value={formData.end_time}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="flex justify-between mt-6">
                            <Button
                                variant="secondary"
                                onClick={() => navigate('/sessions')}
                                type="button"
                            >
                                Cancel
                            </Button>
                            <Button type="submit" disabled={isSaving}>
                                {isSaving ? 'Saving...' : 'Save Session'}
                            </Button>
                        </div>
                    </form>
                )}
            </Card>
        </div>
    );
};

export default SessionFormPage;
