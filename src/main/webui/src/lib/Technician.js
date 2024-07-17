
// Custom Technician resource model, based on ResourceModel with additional fields
import {DateHelper, ResourceModel} from "@bryntum/schedulerpro";

export default class Technician extends ResourceModel {
    static fields = [
        // The skills will be matched against the "skills" field of Tasks
        { name : 'skills', type : 'array' },
        'type',
        { name : 'hoursPerWeek', defaultValue : 40 }
    ];

    getBookedHours(startDate, endDate) {
        let total = 0;
        this.events.forEach(eventRecord => {
            if (DateHelper.intersectSpans(eventRecord.startDate, eventRecord.endDate, startDate, endDate)) {
                total += eventRecord.duration;
            }
        });

        return total;
    }

    canPerformTask(taskRecord, startDate) {
        const
            { events }  = this,
            {
                skills : requiredSkills,
                calendar
            }           = taskRecord,
            endDate     = startDate && DateHelper.add(startDate, taskRecord.duration, taskRecord.durationUnit),
            skillsMatch = !requiredSkills || requiredSkills.every(skillId => this.skills?.includes(skillId)),
            hasEnoughAvailability = Boolean(!startDate || this.getFirstAvailableTimeSlot(startDate, taskRecord));

        return skillsMatch && (!startDate || (
            // Respect technician working time
            (!calendar || calendar.isWorkingTime(startDate, endDate, true)))) &&
            hasEnoughAvailability;
    }

    get skillNames() {
        const skillStore = this.project.getCrudStore('skills');
        return this.skills?.map(id => skillStore.getById(id).name) || '';
    }

    getEventsForDay(date) {
        const startOfDay = DateHelper.clearTime(date);

        return this.getEventsInRange(date, DateHelper.add(date, 1, 'day'));
    }

    getEventsInRange(startDate, endDate) {
        return this.events.filter(eventRecord => DateHelper.intersectSpans(startDate, endDate, eventRecord.startDate, eventRecord.endDate));
    }

    getFirstAvailableTimeSlot(date, taskRecord) {
        date = DateHelper.clearTime(date);

        const availabilityRange = this.effectiveCalendar.getWorkingTimeRanges(date, DateHelper.add(date, 1, 'day'))[0];

        if (availabilityRange) {
            let
                eventsOnDate           = this.getEventsForDay(date);

            if (DateHelper.isSameDate(taskRecord.startDate, date)) {
                eventsOnDate = eventsOnDate.filter(ev => ev !== taskRecord);
            }

            const
                nextStartSlot          = eventsOnDate[eventsOnDate.length - 1]?.endDate || availabilityRange.startDate,
                remainingAvailableTime = availabilityRange.endDate - nextStartSlot;

            if (remainingAvailableTime >= taskRecord.durationMS) {
                return nextStartSlot;
            }
        }
    }
}
