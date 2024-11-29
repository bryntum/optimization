// Custom Task model, based on EventModel with additional fields and changed defaults
import {EventModel} from "@bryntum/schedulerpro";

export default class Task extends EventModel {
    static fields = [
        { name : 'iconCls', defaultValue : 'b-fa b-fa-bus' },
        { name : 'licensePlate', defaultValue : '' },
        // The skills required to perform a task
        { name : 'skills', type : 'array' },
        { name : 'manuallyScheduled', type : 'boolean' },
        { name : 'duration', defaultValue : 1 },
        { name : 'durationUnit', defaultValue : 'h' },
        {
            name: 'startDate',
            serialize(value) {
                return value ? value.toISOString() : null;
            }
        },
        {
            name: 'endDate',
            serialize(value) {
                return value ? value.toISOString() : null;
            }
        }
    ];

    get requiredSkillRecords() {
        const skillStore = this.firstStore.crudManager.getCrudStore('skills');
        return this.skills?.map(id => skillStore.getById(id)) || [];
    }

    get requiredSkillNames() {
        const skillStore = this.firstStore.crudManager.getCrudStore('skills');
        return this.skills?.map(id => skillStore.getById(id).name) || [];
    }

    getColorByLicensePlate() {
        const colors = [
            { background: '#FFB3BA', text: '#CC8A8A' },
            { background: '#FFDFBA', text: '#CCB28A' },
            { background: '#FFFFBA', text: '#CCCC8A' },
            { background: '#BAFFC9', text: '#8ACC9A' },
            { background: '#BAE1FF', text: '#8AB3CC' },
            { background: '#FFB3E6', text: '#CC8AB3' },
            { background: '#FFB347', text: '#CC8A36' },
            { background: '#FF6961', text: '#CC4D4A' },
            { background: '#FFD1DC', text: '#CC9BAA' },
            { background: '#C1E1C1', text: '#91B391' },
            { background: '#F0E68C', text: '#C0B45A' },
            { background: '#E6E6FA', text: '#B3B3CC' },
            { background: '#D8BFD8', text: '#A68FA6' },
            { background: '#FFE4E1', text: '#CCB3B0' },
            { background: '#FFFACD', text: '#CCC99A' },
            { background: '#E0FFFF', text: '#A3CCCC' }
        ];
        let hash = 0;
        for (let i = 0; i < this.licensePlate.length; i++) {
            hash = this.licensePlate.charCodeAt(i) + ((hash << 5) - hash);
        }
        return colors[Math.abs(hash) % colors.length];
    }
}
