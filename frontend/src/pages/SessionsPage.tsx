import React, { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Card, Button, Table, Select } from '../components/UI';
import { getSessions, getClients } from '../services/api';
import { SessionWithDuration, Client } from '../types';
import { PlusIcon } from '@heroicons/react/24/outline';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import { useLanguage } from '../i18n';
import { formatBackendDate } from '../utils/dateUtils';

const SessionsPage: React.FC = () => {
    const [sessions, setSessions] = useState<SessionWithDuration[]>([]);
    const [clients, setClients] = useState<Client[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const { translations } = useLanguage();
    const navigate = useNavigate();

    // Filter states
    const [clientFilter, setClientFilter] = useState<number | ''>('');
    const [startDate, setStartDate] = useState<Date | null>(null);
    const [endDate, setEndDate] = useState<Date | null>(null);

    const fetchSessions = useCallback(async () => {
        setIsLoading(true);

        try {
            const filters: { client_id?: number, start_date?: string, end_date?: string } = {};

            if (clientFilter !== '') {
                filters.client_id = Number(clientFilter);
            }

            if (startDate) {
                filters.start_date = startDate.toISOString().split('T')[0];
            }

            if (endDate) {
                filters.end_date = endDate.toISOString().split('T')[0];
            }

            const data = await getSessions(filters);
            setSessions(data);
            setError('');
        } catch (error) {
            console.error('Error fetching sessions:', error);
            setError(translations.common.errors.failedToLoad);
        } finally {
            setIsLoading(false);
        }
    }, [clientFilter, startDate, endDate, translations.common.errors.failedToLoad]);

    useEffect(() => {
        const fetchData = async () => {
            try {
                // Fetch clients for filter dropdown
                const clientsData = await getClients();
                setClients(clientsData);

                // Initial fetch of all sessions
                await fetchSessions();
            } catch (error) {
                console.error('Error fetching data:', error);
                setError(translations.common.errors.failedToLoad);
            } finally {
                setIsLoading(false);
            }
        };

        fetchData();
    }, [fetchSessions, translations.common.errors.failedToLoad]);

    const handleFilter = () => {
        fetchSessions();
    };

    const handleClearFilters = () => {
        setClientFilter('');
        setStartDate(null);
        setEndDate(null);

        // Fetch all sessions without filters
        fetchSessions();
    };

    const columns = [
        { key: 'date', label: translations.sessions.columns.date },
        { key: 'client_name', label: translations.sessions.columns.client },
        { key: 'name', label: translations.sessions.form.labels.description },
        { key: 'time', label: translations.sessions.form.labels.startTime },
        { key: 'duration', label: translations.sessions.columns.duration },
    ];

    const formattedSessions = sessions.map((item) => {
        // Handle case where the entire item might be malformed
        if (!item) {
            console.warn('Found null or undefined session item');
            return {
                id: 'unknown',
                date: 'N/A',
                client_name: 'Unknown',
                name: 'Unknown session',
                time: 'N/A',
                duration: 'N/A',
            };
        }

        const { client_name, duration_minutes } = item;

        // Based on the console output, it appears the session data is directly on the item object
        // rather than nested inside a 'session' property
        return {
            id: item.id,
            date: formatBackendDate(item.date),
            client_name,
            name: item.name || 'Unnamed session',
            time: `${item.start_time || '??:??'} - ${item.end_time || '??:??'}`,
            duration: duration_minutes ? `${Math.floor(duration_minutes / 60)}h ${duration_minutes % 60}m` : 'N/A',
        };
    });

    return (
        <div>
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">{translations.sessions.title}</h1>
                <Link to="/sessions/new">
                    <Button className="flex items-center">
                        <PlusIcon className="w-5 h-5 mr-1" />
                        {translations.sessions.addNew}
                    </Button>
                </Link>
            </div>

            <Card className="mb-6">
                <h2 className="text-lg font-semibold mb-4">{translations.common.filter}</h2>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <Select
                        id="client-filter"
                        label={translations.sessions.columns.client}
                        value={clientFilter}
                        onChange={(e) => setClientFilter(e.target.value as number | '')}
                        options={[
                            { value: '', label: `${translations.common.all} ${translations.navigation.clients}` },
                            ...clients.map(client => ({
                                value: client.id,
                                label: client.name
                            }))
                        ]}
                    />

                    <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            {translations.sessions.columns.date} (von)
                        </label>
                        <DatePicker
                            selected={startDate}
                            onChange={(date: Date | null) => setStartDate(date)}
                            className="w-full p-2 border border-gray-300 rounded-md"
                            dateFormat="dd.MM.yyyy"
                            isClearable
                            placeholderText="Startdatum auswählen"
                        />
                    </div>

                    <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            {translations.sessions.columns.date} (bis)
                        </label>
                        <DatePicker
                            selected={endDate}
                            onChange={(date: Date | null) => setEndDate(date)}
                            className="w-full p-2 border border-gray-300 rounded-md"
                            dateFormat="dd.MM.yyyy"
                            isClearable
                            placeholderText="Enddatum auswählen"
                            minDate={startDate || undefined}
                        />
                    </div>
                </div>

                <div className="flex justify-end space-x-2 mt-2">
                    <Button variant="secondary" onClick={handleClearFilters}>
                        Filter zurücksetzen
                    </Button>
                    <Button onClick={handleFilter}>
                        Filter anwenden
                    </Button>
                </div>
            </Card>

            {isLoading ? (
                <div className="flex justify-center items-center h-64">
                    <p className="text-gray-600">Loading sessions...</p>
                </div>
            ) : error ? (
                <Card className="text-center py-8">
                    <p className="text-red-500">{error}</p>
                    <Button
                        variant="secondary"
                        className="mt-4"
                        onClick={() => fetchSessions()}
                    >
                        Try Again
                    </Button>
                </Card>
            ) : (
                <Card>
                    <Table
                        columns={columns}
                        data={formattedSessions}
                        onRowClick={(row) => {
                            navigate(`/sessions/${row.id}`);
                        }}
                    />
                </Card>
            )}
        </div>
    );
};

export default SessionsPage;
