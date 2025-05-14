import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Card, Button, Table, Select } from '../components/UI';
import { getSessions, getClients } from '../services/api';
import { SessionWithDuration, Client } from '../types';
import { PlusIcon } from '@heroicons/react/24/outline';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";

const SessionsPage: React.FC = () => {
    const [sessions, setSessions] = useState<SessionWithDuration[]>([]);
    const [clients, setClients] = useState<Client[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    // Filter states
    const [clientFilter, setClientFilter] = useState<number | ''>('');
    const [startDate, setStartDate] = useState<Date | null>(null);
    const [endDate, setEndDate] = useState<Date | null>(null);

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
                setError('Failed to load data. Please try again later.');
            } finally {
                setIsLoading(false);
            }
        };

        fetchData();
    }, []);

    const fetchSessions = async () => {
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
            setError('Failed to load sessions. Please try again.');
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
        { key: 'date', label: 'Date' },
        { key: 'client_name', label: 'Client' },
        { key: 'name', label: 'Session Name' },
        { key: 'time', label: 'Time' },
        { key: 'duration', label: 'Duration' },
    ];

    const formattedSessions = sessions.map((item) => {
        const { session, client_name, duration_minutes } = item;
        return {
            id: session.id,
            date: new Date(session.date).toLocaleDateString(),
            client_name,
            name: session.name,
            time: `${session.start_time} - ${session.end_time}`,
            duration: `${Math.floor(duration_minutes / 60)}h ${duration_minutes % 60}m`,
        };
    });

    return (
        <div>
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Sessions</h1>
                <Link to="/sessions/new">
                    <Button className="flex items-center">
                        <PlusIcon className="w-5 h-5 mr-1" />
                        Log New Session
                    </Button>
                </Link>
            </div>

            <Card className="mb-6">
                <h2 className="text-lg font-semibold mb-4">Filter Sessions</h2>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <Select
                        id="client-filter"
                        label="Client"
                        value={clientFilter}
                        onChange={(e) => setClientFilter(e.target.value as number | '')}
                        options={[
                            { value: '', label: 'All Clients' },
                            ...clients.map(client => ({
                                value: client.id,
                                label: client.name
                            }))
                        ]}
                    />

                    <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Start Date
                        </label>
                        <DatePicker
                            selected={startDate}
                            onChange={(date: Date | null) => setStartDate(date)}
                            className="w-full p-2 border border-gray-300 rounded-md"
                            dateFormat="yyyy-MM-dd"
                            isClearable
                            placeholderText="Select start date"
                        />
                    </div>

                    <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            End Date
                        </label>
                        <DatePicker
                            selected={endDate}
                            onChange={(date: Date | null) => setEndDate(date)}
                            className="w-full p-2 border border-gray-300 rounded-md"
                            dateFormat="yyyy-MM-dd"
                            isClearable
                            placeholderText="Select end date"
                            minDate={startDate || undefined}
                        />
                    </div>
                </div>

                <div className="flex justify-end space-x-2 mt-2">
                    <Button variant="secondary" onClick={handleClearFilters}>
                        Clear Filters
                    </Button>
                    <Button onClick={handleFilter}>
                        Apply Filters
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
                    />
                </Card>
            )}
        </div>
    );
};

export default SessionsPage;
