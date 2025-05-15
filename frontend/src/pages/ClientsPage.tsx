import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Card, Button, Table } from '../components/UI';
import { getClients } from '../services/api';
import { Client } from '../types';
import { PlusIcon } from '@heroicons/react/24/outline';
import { useLanguage } from '../i18n';

const ClientsPage: React.FC = () => {
    const [clients, setClients] = useState<Client[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const navigate = useNavigate();
    const { translations } = useLanguage();

    useEffect(() => {
        const fetchClients = async () => {
            try {
                const data = await getClients();
                setClients(data);
                setError('');
            } catch (error) {
                console.error('Error fetching clients:', error);
                setError(translations.common.errors.failedToLoad);
            } finally {
                setIsLoading(false);
            }
        };

        fetchClients();
    }, [translations.common.errors.failedToLoad]);

    const columns = [
        { key: 'name', label: translations.clients.columns.name },
        { key: 'contact_person', label: translations.clients.columns.contactPerson },
        { key: 'default_hourly_rate', label: translations.clients.columns.hourlyRate },
    ];

    const formattedClients = clients.map((client) => ({
        id: client.id,
        name: client.name,
        contact_person: client.contact_person || '-',
        default_hourly_rate: `€${client.default_hourly_rate.toFixed(2)}`,
    }));

    return (
        <div>
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">{translations.clients.title}</h1>
                <Link to="/clients/new">
                    <Button className="flex items-center">
                        <PlusIcon className="w-5 h-5 mr-1" />
                        {translations.clients.addNew}
                    </Button>
                </Link>
            </div>

            {isLoading ? (
                <div className="flex justify-center items-center h-64">
                    <p className="text-gray-600">{translations.common.loading}...</p>
                </div>
            ) : error ? (
                <Card className="text-center py-8">
                    <p className="text-red-500">{error}</p>
                    <Button
                        variant="secondary"
                        className="mt-4"
                        onClick={() => window.location.reload()}
                    >
                        Try Again
                    </Button>
                </Card>
            ) : (
                <Card>
                    <Table
                        columns={columns}
                        data={formattedClients}
                        onRowClick={(row) => {
                            navigate(`/clients/${row.id}`);
                        }}
                    />
                </Card>
            )}
        </div>
    );
};

export default ClientsPage;
