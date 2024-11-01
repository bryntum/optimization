import { StringHelper, DateHelper } from "@bryntum/schedulerpro";

/**
 * Application configuration
 */
const useSchedulerProConfig = (onSolve, onReset) => {
    return {
        multiEventSelect: true,

        startDate : new Date(2024, 10, 4),
        endDate   : new Date(2024, 10, 9),
        flex      : 1,

        timeResolution: {
            magnitude: 1,
            unit: 'd'
        },
        eventStyle: 'rounded',
        rowHeight: 65,
        barMargin: 7,
        tickSize: 150,
        fillTicks: true,
        eventColor: 'indigo',
        useInitialAnimation: false,
        zoomOnMouseWheel: false,
        zoomOnTimeAxisDoubleClick: false,

        title: 'Planned maintenance activities',
        ui: 'toolbar',
        tools : [
            {
                type     : 'button',
                text     : 'Reset',
                icon     : 'b-icon b-fa-rotate-right',
                cls      : 'b-transparent',
                tooltip  : 'Resets the data',
                onAction : onReset 
            },
            {
                type     : 'button',
                text     : 'Solve',
                icon     : 'b-icon b-fa-wand-magic-sparkles',
                cls      : 'b-transparent',
                tooltip  : 'Tries to fit the unplanned events into the currently displayed timeframe',
                onAction : async ({ source }) => {
                    source.icon = 'b-icon b-fa-spinner'
                    await onSolve()
                    source.icon = 'b-icon b-fa-check'
                }
            },
        ],

        calendarHighlightFeature: {
            calendar : 'resource',
            inflate  : {
                x : -8,
                y : -1
            },
            collectAvailableResources({ scheduler, eventRecords }) {
                return scheduler.resourceStore.query(technician => technician.canPerformTask(eventRecords[0]));
            }
        },

        viewPreset: {
            base: 'dayAndWeek',
            shiftUnit: 'week',
            headers: [
                {
                    unit: 'd',
                    align: 'center',
                    dateFormat: 'ddd DD'
                }
            ]
        },

        columns: [
            {
                type           : 'resourceInfo',
                text           : 'Staff',
                width          : 300,
                showEventCount : false,
                // Show skills each technician has
                showMeta(resourceRecord) {
                    const
                        { skillNames, role }   = resourceRecord,
                        { startDate, endDate } = this.grid,
                        bookedHours            = resourceRecord.getBookedHours(startDate, endDate),
                        overAllocated          = bookedHours > resourceRecord.hoursPerWeek;

                    return `<ul class="skills">${skillNames.map(skill => `<li>${StringHelper.encodeHtml(skill)}</li>`).join('')}</ul>
                        <div data-btip="${bookedHours}h / ${resourceRecord.hoursPerWeek} allocated"><i class="b-fa ${overAllocated ? 'b-fa-triangle-exclamation' : 'b-fa-clock'}"></i>${bookedHours} / ${resourceRecord.hoursPerWeek}</div>`;
                },
                filterable : {
                    filterField : {
                        triggers : {
                            search : {
                                cls : 'b-icon b-fa-filter'
                            }
                        },
                        placeholder : 'Staff'
                    }
                }
            }
        ],

        eventTooltipFeature : {
            template({ eventRecord }) {
                return `<div class="field"><label>Task</label><span>${StringHelper.encodeHtml(eventRecord.name)}</span></div>
                    <div class="field"><label>Required skills</label><ul class="skills">${eventRecord.requiredSkillNames.map(skill => `<li>${skill}</li>`).join('')}</ul></div>
                    <div class="field"><label>Start</label><span>${DateHelper.format(eventRecord.startDate, 'MMM DD LST')}</span></div>
                    <div class="field"><label>Duration</label><span>${eventRecord.fullDuration}</span></div>
                    <div class="field"><label>Assigned to</label><span>${StringHelper.encodeHtml(eventRecord.resource.name)}</span></div>
                    <div class="field"><label>Manually Assigned</label><span>${eventRecord.manuallyScheduled ? 'Yes' : 'No'}</span></div>
                `;
            }
        },
        eventRenderer({ eventRecord }) {
            return `
                <div>
                    <div class="b-event-header">
                        <div class="b-event-name">${eventRecord.name}</div>
                        <div class="b-event-duration">${eventRecord.fullDuration.toString(true)}</div>
                    </div>
                    <div class="license-plate">
                        <div>Vehicle: ${eventRecord.licensePlate}</div>
                    </div>
                    ${eventRecord.manuallyScheduled ? '<div class="manually-scheduled">M</div>' : ''}
                </div>
            `
        }
    }
};

const useUnplannedGridConfig = (onAddRandomTasks) => {
    return {
        cls                        : 'b-unplanned-grid',
        hideHeaders                : true,
        disableGridRowModelWarning : true,
        collapsible                : true,
        flex                       : '0 0 300px',
        ui                         : 'toolbar',
        title                      : 'Unplanned maintenance',
        emptyText                  : 'No unplanned maintenance',
        selectionMode              : {
            multiSelect : false
        },
        features : {
            stripe : true,
            sort   : 'name'
        },

        tools : [
            {
                type : 'button',
                text : '+5',
                tooltip : 'Add 5 random tasks',
                onAction : () => onAddRandomTasks(5)
            }
        ],

        columns : [
            {
                flex       : 1,
                field      : 'name',
                cellCls    : 'unscheduledNameCell',
                autoHeight : true,
                htmlEncode : false,
                renderer   : ({ record : task }) => `
                        <div class="vehicle-ct">
                            <i class="${StringHelper.encodeHtml(task.iconCls) || ''}"></i>
                            <span class="license-plate">${StringHelper.encodeHtml(task.licensePlate)}</span>
                        </div>
                        <div class="name-container">
                            <div class="main-info"><span class="task-name">${StringHelper.encodeHtml(task.name)}</span></div>
                            <div class="meta-info"><ul class="skills">${task.requiredSkillNames.map(skill => `<li data-btip="This task requires a technician with the following skills: <strong>${task.requiredSkillNames.join(', ')}</strong>">${skill}</li>`).join('')}</ul><span class="duration">${task.duration ? task.duration + 'h' : ''}</span></div>
                        </div>
                    `
            }
        ],
    };
};

export { useSchedulerProConfig, useUnplannedGridConfig };
