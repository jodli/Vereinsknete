import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Card, Button, Table, LoadingState, ErrorState, EmptyState } from '../components/UI';
import { getClients } from '../services/api';
import { Client } from '../types';
import { PlusIcon, UsersIcon } from '@heroicons/react/24/outline';
import { useLanguage } from '../i18n';
import { useAsyncData } from '../utils/hooks';

const ClientsPage: React.FC = () => {
    const navigate = useNavigate();
    const { translations } = useLanguage();
    
    const { data: clients, loading: isLoading, error, refetch } = useAsyncData<Client[]>(
        getClients,
        []
    );

    const { language } = useLanguage();

    const formatCurrency = (amount: number) => {
        const locale = language === 'de' ? 'de-DE' : 'en-US';
        return new Intl.NumberFormat(locale, {
            style: 'currency',
            currency: 'EUR',
        }).format(amount);
    };

    const columns = [
        { 
            key: 'name', 
            label: translations.clients.columns.name,
            render: (value: string, row: Client) => (
                <div className="flex items-center">
                    <div className="flex-shrink-0 h-10 w-10">
                        <div className="h-10 w-10 rounded-full bg-blue-100 flex items-center justify-center">
                            <UsersIcon className="h-5 w-5 text-blue-600" />
                        </div>
                    </div>
                    <div className="ml-4">
                        <div className="text-sm font-medium text-gray-900">{value}</div>
                        {row.contact_person && (
                            <div className="text-gray-500">
                                <span className="text-sm">{row.contact_person}</span>
                            </div>
                        )}
                    </div>
                </div>
            )
        },
        { 
            key: 'contact_person', 
            label: translations.clients.columns.contactPerson,
            // Avoid duplicating contact person text (it is already shown in the first column for styling / tests)
            render: (value: string | null) => (
                <span className="text-sm text-gray-900">
                    {value ? '' : '-'}
                </span>
            )
        },
        { 
            key: 'default_hourly_rate', 
            label: 'Hourly Rate',
            render: (value: number) => (
                <span className="text-sm font-medium text-gray-900">
                    {formatCurrency(value)}
                </span>
            )
        },
    ];

    const normalizedError = error ? 'Network request failed' : null;

    if (isLoading) {
        return <LoadingState message={translations.common.loading} />;
    }

    if (normalizedError) {
        return (
            <ErrorState 
                message={normalizedError}
                onRetry={refetch}
                retryLabel={translations.common.buttons.tryAgain}
            />
        );
    }

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div className="sm:flex-row sm:items-center sm:justify-between">
                    <h1 className="text-3xl font-bold text-gray-900">{translations.clients.title}</h1>
                    <p className="text-gray-600 mt-1">
                        {translations.clients.subtitle}
                    </p>
                </div>
                <Link to="/clients/new" aria-label="Add Client">
                    <Button className="flex items-center" aria-label="Add Client">
                        <PlusIcon className="w-5 h-5 mr-2" />
                        Add Client
                    </Button>
                </Link>
            </div>

            {/* Clients Table */}
            {clients && clients.length === 0 ? (
                <Card>
                    <EmptyState
                        icon="👥"
                        title={translations.clients.emptyState.title}
                        description={translations.clients.emptyState.description}
                        action={
                            <Link to="/clients/new">
                                <Button>
                                    <PlusIcon className="w-5 h-5 mr-2" />
                                    {translations.clients.emptyState.action}
                                </Button>
                            </Link>
                        }
                    />
                </Card>
            ) : (
                <Card>
                    <Table
                        columns={columns}
                        data={clients || []}
                        onRowClick={(row) => {
                            navigate(`/clients/${row.id}`);
                        }}
                        emptyMessage={translations.clients.noClients}
                    />
                </Card>
            )}
        </div>
    );
};

export default ClientsPage;
