import React from 'react';
import axios from 'axios';

export default class ApplicationPage extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            dataRecords: [],
            isLoading: true,
        };
    }

    componentDidMount() {
        axios
            .get('/api/data-records/0b12c601-9287-3f5c-a78c-df508fe0f889/2020/01')
            .then(dataRecords => this.setState({ dataRecords: dataRecords.data, isLoading: false }));
    }

    render() {
        const { dataRecords, isLoading } = this.state;
        if (isLoading) {
            return <div>Loading...</div>
        }
        return (
            <table style={{ width: '100%' }}>
                <caption>Data Records</caption>
                <tr>
                    <th>Recorded At</th>
                    <th>Internal Record ID</th>
                    <th>Recorded Bytes</th>
                </tr>
                {dataRecords.map(dataRecord => (
                    <tr key={dataRecord.key.internalRecordId}>
                        <td>{dataRecord.key.recordedAt}</td>
                        <td>{dataRecord.key.internalRecordId}</td>
                        <td>{dataRecord.recordedBytes}</td>
                    </tr>
                ))}
            </table>
        );
    }
}