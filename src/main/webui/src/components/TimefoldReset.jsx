import React, { useState } from 'react';

const TimefoldReset = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [data, setData] = useState(null);

    const fetchData = async () => {
        setIsLoading(true);
        try {
            const response = await fetch('./api/reset', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }});
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();
            setData(data);
        } catch (error) {
            console.error('Error fetching data:', error);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div>
            <button onClick={fetchData} disabled={isLoading}>
                {isLoading ? 'Loading...' : 'Reset'}
            </button>
            {data && (
                <div>
                    <h3>Data:</h3>
                    <pre>{JSON.stringify(data, null, 2)}</pre>
                </div>
            )}
        </div>
    );
};

export default TimefoldReset;