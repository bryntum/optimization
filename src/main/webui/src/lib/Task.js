
// Custom Task model, based on EventModel with additional fields and changed defaults
import {EventModel} from "@bryntum/schedulerpro";

export default class Task extends EventModel {
    static fields = [
        { name : 'iconCls', defaultValue : 'b-fa b-fa-bus' },
        { name : 'licensePlate', defaultValue : '' },
        // The skills required to perform a task
        { name : 'skills', type : 'array' },
        { name : 'duration', defaultValue : 1 },
        { name : 'durationUnit', defaultValue : 'h' },
            {
                name : 'startDate',
                serialize(value) {
                    return value.toISOString();
                }
            },
            {
                name : 'endDate',
                serialize(value) {
                    return value.toISOString();
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
}
