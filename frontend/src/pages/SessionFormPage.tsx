import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Input, Select, Button, LoadingState, ErrorState } from '../components/UI';
import { getClients, createSession, getSession, updateSession, deleteSession } from '../services/api';
import { Client } from '../types';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import { useLanguage } from '../i18n';
import { useToast } from '../components/Toast';
import { useConfirm } from '../components/ConfirmDialog';
import { useFormState } from '../utils/hooks';
import { TrashIcon, UserIcon, CalendarIcon, ClockIcon } from '@heroicons/react/24/outline';
import { parseGermanDateString } from '../utils/dateUtils';

const SessionFormPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const isEditing = id && id !== 'new';
    const { translations } = useLanguage();
    const { success, error: showError } = useToast();
    const { confirm } = useConfirm();

    const [clients, setClients] = useState<Client[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [error, setError] = useState('');

    const initialFormData = {
        client_id: '',
        name: '',
        date: new Date(),
        start_time: '',
        end_time: '',
    };

    const validationRules = {
        client_id: (value: string) => {
            if (!value) return 'Client is required';
            return null;
        },
        name: (value: string) => {
            if (!value.trim()) return 'Description is required';
            if (value.length < 2) return 'Description must be at least 2 characters';
            return null;
        },
        start_time: (value: string) => {
            if (!value) return 'Start time is required';
            return null;
        },
        end_time: (value: string) => {
            if (!value) return 'End time is required';
            return null;
        },
    };

    const {
        formData,
        errors,
        touched,
        setFieldValue,
        setFieldTouched,
        validateForm,
    } = useFormState(initialFormData, validationRules);

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
                    setFieldValue('client_id', sessionData.client_id.toString());
                    setFieldValue('name', sessionData.name);
                    setFieldValue('date', parsedDate || new Date());
                    setFieldValue('start_time', sessionData.start_time);
                    setFieldValue('end_time', sessionData.end_time);
                } else if (clientsData.length > 0) {
                    setFieldValue('client_id', clientsData[0].id.toString());
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
        setFieldValue(name as keyof typeof initialFormData, value);
        
        // Custom validation for end_time vs start_time
        if (name === 'end_time' && formData.start_time && value <= formData.start_time) {
            // This will be handled by the form validation
        }
        if (name === 'start_time' && formData.end_time && formData.end_time <= value) {
            // This will be handled by the form validation
        }
    };

    const handleBlur = (e: React.FocusEvent<HTMLInputElement | HTMLSelectElement>) => {
        const fieldName = e.target.name as keyof typeof initialFormData;
        setFieldTouched(fieldName, true);
    };

    const handleDateChange = (date: Date | null) => {
        if (date) {
            setFieldValue('date', date);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        
        if (!validateForm()) {
            return;
        }

        // Additional validation for time comparison
        if (formData.start_time && formData.end_time && formData.end_time <= formData.start_time) {
            showError('Invalid time range', 'End time must be after start time');
            return;
        }

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
                success('Session updated', 'Session has been successfully updated.');
            } else {
                await createSession(sessionData);
                success('Session created', 'New session has been successfully added.');
            }

            navigate('/sessions');
        } catch (error) {
            console.error('Error saving session:', error);
            const errorMessage = isEditing ? 'Failed to update session' : 'Failed to create session';
            showError(errorMessage, error instanceof Error ? error.message : 'An unexpected error occurred');
            setIsSaving(false);
        }
    };

    const handleDelete = () => {
        confirm({
            title: 'Delete Session',
            message: `Are you sure you want to delete "${formData.name}"? This action cannot be undone.`,
            confirmLabel: 'Delete Session',
            variant: 'danger',
            onConfirm: async () => {
                setIsDeleting(true);
                setError('');

                try {
                    if (id) {
                        await deleteSession(parseInt(id));
                        success('Session deleted', 'Session has been successfully removed.');
                        navigate('/sessions');
                    }
                } catch (error) {
                    console.error('Error deleting session:', error);
                    showError('Failed to delete session', error instanceof Error ? error.message : 'An unexpected error occurred');
                    setIsDeleting(false);
                }
            },
        });
    };

    if (isLoading) {
        return <LoadingState message={translations.common.loading} />;
    }

    if (error && !isSaving && !isDeleting) {
        return (
            <ErrorState 
                message={error}
                onRetry={() => window.location.reload()}
                retryLabel={translations.common.buttons.tryAgain}
            />
        );
    }

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">
                        {isEditing ? translations.sessions.form.title.edit : translations.sessions.form.title.new}
                    </h1>
                    <p className="text-gray-600 mt-1">
                        {isEditing 
                            ? translations.sessions.form.subtitle.edit
                            : translations.sessions.form.subtitle.new
                        }
                    </p>
                </div>
                {isEditing && (
                    <Button
                        variant="danger"
                        onClick={handleDelete}
                        disabled={isDeleting}
                        loading={isDeleting}
                        className="flex items-center"
                    >
                        <TrashIcon className="w-5 h-5 mr-2" />
                        {translations.sessions.form.buttons.delete}
                    </Button>
                )}
            </div>

            {/* Form */}
            <div className="max-w-2xl">
                <Card>
                    {error && (isSaving || isDeleting) && (
                        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
                            <p className="text-red-800">{error}</p>
                        </div>
                    )}

                    {clients.length === 0 ? (
                        <div className="text-center py-12">
                            <CalendarIcon className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                            <h3 className="text-lg font-medium text-gray-900 mb-2">No clients available</h3>
                            <p className="text-gray-600 mb-6">{translations.sessions.form.noClients}</p>
                            <Button onClick={() => navigate('/clients/new')}>
                                <UserIcon className="w-5 h-5 mr-2" />
                                {translations.dashboard.setupSteps.addClient.title}
                            </Button>
                        </div>
                    ) : (
                        <form onSubmit={handleSubmit} className="space-y-6">
                            {/* Session Details */}
                            <fieldset className="border border-gray-200 rounded-lg p-6">
                                <legend className="text-lg font-medium text-gray-900 px-2">
                                    {translations.sessions.form.sections.sessionDetails}
                                </legend>
                                <div className="space-y-4 mt-4">
                                    <Select
                                        id="client_id"
                                        name="client_id"
                                        label={translations.sessions.form.labels.client}
                                        value={formData.client_id}
                                        onChange={handleChange}
                                        error={touched.client_id ? errors.client_id : undefined}
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
                                        onBlur={handleBlur}
                                        error={touched.name ? errors.name : undefined}
                                        placeholder={translations.sessions.form.placeholders.description}
                                        helpText="Brief description of the work performed"
                                        required
                                    />
                                </div>
                            </fieldset>

                            {/* Date & Time */}
                            <fieldset className="border border-gray-200 rounded-lg p-6">
                                <legend className="text-lg font-medium text-gray-900 px-2">
                                    {translations.sessions.form.sections.dateTime}
                                </legend>
                                <div className="space-y-4 mt-4">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            {translations.sessions.form.labels.date}
                                            <span className="text-red-500 ml-1">*</span>
                                        </label>
                                        <div className="relative">
                                            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                                <CalendarIcon className="h-5 w-5 text-gray-400" />
                                            </div>
                                            <DatePicker
                                                selected={formData.date}
                                                onChange={handleDateChange}
                                                className="w-full pl-10 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                                dateFormat="dd.MM.yyyy"
                                                required
                                            />
                                        </div>
                                        <p className="text-sm text-gray-500 mt-1">Select the date when the work was performed</p>
                                    </div>

                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        <Input
                                            id="start_time"
                                            name="start_time"
                                            label={translations.sessions.form.labels.startTime}
                                            type="time"
                                            value={formData.start_time}
                                            onChange={handleChange}
                                            onBlur={handleBlur}
                                            error={touched.start_time ? errors.start_time : undefined}
                                            leftIcon={<ClockIcon />}
                                            required
                                        />

                                        <Input
                                            id="end_time"
                                            name="end_time"
                                            label={translations.sessions.form.labels.endTime}
                                            type="time"
                                            value={formData.end_time}
                                            onChange={handleChange}
                                            onBlur={handleBlur}
                                            error={touched.end_time ? errors.end_time : undefined}
                                            leftIcon={<ClockIcon />}
                                            required
                                        />
                                    </div>
                                </div>
                            </fieldset>

                            {/* Form Actions */}
                            <div className="flex flex-col sm:flex-row sm:justify-between gap-4 pt-6 border-t border-gray-200">
                                <Button
                                    variant="secondary"
                                    onClick={() => navigate('/sessions')}
                                    type="button"
                                >
                                    {translations.common.cancel}
                                </Button>
                                <Button 
                                    type="submit" 
                                    disabled={isSaving}
                                    loading={isSaving}
                                >
                                    {translations.sessions.form.buttons.save}
                                </Button>
                            </div>
                        </form>
                    )}
                </Card>
            </div>
        </div>
    );
};

export default SessionFormPage;
