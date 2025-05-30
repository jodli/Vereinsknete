import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Input, Select, Button } from '../components/UI';
import { getClients, createSession, getSession, updateSession, deleteSession } from '../services/api';
import { Client } from '../types';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import { useLanguage } from '../i18n';
import { TrashIcon } from '@heroicons/react/24/outline';
import { parseGermanDateString } from '../utils/dateUtils';

const SessionFormPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const isEditing = id && id !== 'new';
    const { translations } = useLanguage();

    const [clients, setClients] = useState<Client[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [error, setError] = useState('');

    const [formData, setFormData] = useState({
        client_id: '',
        name: '',
        date: new Date(),
        start_time: '',
        end_time: '',
    });

    useEffect(() => {
        const fetchData = async () => {
            try {
                // Fetch clients for dropdown
                const clientsData = await getClients();
                setClients(clientsData);

                // If editing, fetch the session data
                if (isEditing && id) {
                    const sessionData = await getSession(parseInt(id));
                    const parsedDate = parseGermanDateString(sessionData.date);
                    setFormData({
                        client_id: sessionData.client_id.toString(),
                        name: sessionData.name,
                        date: parsedDate || new Date(), // Fallback to current date if parsing fails
                        start_time: sessionData.start_time,
                        end_time: sessionData.end_time,
                    });
                } else if (clientsData.length > 0) {
                    setFormData(prev => ({ ...prev, client_id: clientsData[0].id.toString() }));
                }
                setError('');
            } catch (error) {
                console.error('Error fetching data:', error);
                setError(translations.common.errors.failedToLoad);
            } finally {
                setIsLoading(false);
            }
        };

        fetchData();
    }, [id, isEditing, translations.common.errors.failedToLoad]);

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

            const sessionData = {
                client_id: parseInt(formData.client_id),
                name: formData.name,
                date: formattedDate,
                start_time: formData.start_time,
                end_time: formData.end_time,
            };

            if (isEditing && id) {
                await updateSession(parseInt(id), sessionData);
            } else {
                await createSession(sessionData);
            }

            navigate('/sessions');
        } catch (error) {
            console.error('Error saving session:', error);
            setError(translations.common.errors.failedToSave);
            setIsSaving(false);
        }
    };

    const handleDelete = async () => {
        if (!window.confirm(translations.sessions.form.confirmDelete)) {
            return;
        }

        setIsDeleting(true);
        setError('');

        try {
            if (id) {
                await deleteSession(parseInt(id));
                navigate('/sessions');
            }
        } catch (error) {
            console.error('Error deleting session:', error);
            setError(translations.common.errors.failedToDelete);
            setIsDeleting(false);
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
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">
                    {isEditing ? translations.sessions.form.title.edit : translations.sessions.addNew}
                </h1>
                {isEditing && (
                    <Button
                        variant="danger"
                        onClick={handleDelete}
                        disabled={isDeleting}
                        className="flex items-center"
                    >
                        <TrashIcon className="w-5 h-5 mr-1" />
                        {isDeleting ? translations.common.loading : translations.sessions.form.buttons.delete}
                    </Button>
                )}
            </div>

            <Card>
                {error && (
                    <div className="mb-4 p-2 bg-red-100 text-red-800 rounded">
                        {error}
                    </div>
                )}

                {clients.length === 0 ? (
                    <div className="text-center py-6">
                        <p className="text-gray-700 mb-4">{translations.sessions.form.noClients}</p>
                        <Button onClick={() => navigate('/clients/new')}>
                            {translations.dashboard.setupSteps.addClient.title}
                        </Button>
                    </div>
                ) : (
                    <form onSubmit={handleSubmit}>
                        <Select
                            id="client_id"
                            name="client_id"
                            label={translations.sessions.form.labels.client}
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
                            label={translations.sessions.form.labels.description}
                            value={formData.name}
                            onChange={handleChange}
                            placeholder={translations.sessions.form.placeholders.description}
                            required
                        />

                        <div className="mb-4">
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                {translations.sessions.form.labels.date}<span className="text-red-500">*</span>
                            </label>
                            <DatePicker
                                selected={formData.date}
                                onChange={handleDateChange}
                                className="w-full p-2 border border-gray-300 rounded-md"
                                dateFormat="dd.MM.yyyy"
                                required
                            />
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <Input
                                id="start_time"
                                name="start_time"
                                label={translations.sessions.form.labels.startTime}
                                type="time"
                                value={formData.start_time}
                                onChange={handleChange}
                                required
                            />

                            <Input
                                id="end_time"
                                name="end_time"
                                label={translations.sessions.form.labels.endTime}
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
                                {translations.common.cancel}
                            </Button>
                            <Button type="submit" disabled={isSaving}>
                                {isSaving ? `${translations.common.loading}...` : translations.sessions.form.buttons.save}
                            </Button>
                        </div>
                    </form>
                )}
            </Card>
        </div>
    );
};

export default SessionFormPage;
