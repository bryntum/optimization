import { DragHelper, StringHelper, DomHelper, Toast} from '@bryntum/schedulerpro';

// Handles dragging unscheduled appointment from the grid onto the schedule
export default class Drag extends DragHelper {
    static get defaultConfig() {
        return {
            callOnFunctions      : true,
            autoSizeClonedTarget : false,
            unifiedProxy         : true,
            swapWhenDropOnEvent  : true,
            // Prevent removing proxy on drop, we adopt it for usage in the Schedule
            removeProxyAfterDrop : false,

            // Don't drag the actual row element, clone it
            cloneTarget        : true,
            // Only allow drops on the schedule area
            dropTargetSelector : '.b-timeline-subgrid .b-grid-row:not(.b-group-row),.b-sch-event-wrap',
            // Only allow drag of row elements inside on the unplanned grid
            targetSelector     : '.b-grid-row:not(.b-group-row)'
        };
    }

    afterConstruct(config) {
        // Configure DragHelper with schedule's scrollManager to allow scrolling while dragging
        this.scrollManager = this.schedule.scrollManager;
    }

    createProxy(grabbedElement, initialXY) {
        const
            { context, schedule, grid } = this,
            draggedAppointment          = grid.getRecordFromElement(grabbedElement),
            proxy                       = document.createElement('div');

        proxy.style.cssText = '';
        proxy.style.width   = schedule.tickSize - (2 * schedule.resourceMargin) + 'px';
        proxy.style.height  = schedule.rowHeight - (2 * schedule.resourceMargin) + 'px';

        // Fake an event bar
        proxy.classList.add('b-sch-event-wrap', 'b-sch-style-rounded', 'b-sch-horizontal');
        proxy.innerHTML = StringHelper.xss`
            <div class="b-sch-event b-has-content b-sch-event-withicon">
                <div class="b-sch-event-content">
                    <i class="b-icon b-${draggedAppointment.iconCls}"></i>
                    <div>
                        <div>${draggedAppointment.name}</div>
                    </div>
                </div>
            </div>
        `;

        return proxy;
    }

    onDragStart({ context }) {
        const
            me                 = this,
            { schedule, grid } = me,
            { selectedRecord } = grid,
            task               = grid.getRecordFromElement(context.grabbed);

        // save a reference to the task being dragged so we can access them later
        context.task = selectedRecord;
        schedule.enableScrollingCloseToEdges(schedule.timeAxisSubGrid);

        // Prevent tooltips from showing while dragging
        schedule.features.eventTooltip.disabled = true;
    }

    onDrag({ context }) {
        const
            { schedule } = this,
            { task }     = context,
            newStartDate = schedule.getDateFromCoordinate(context.newX, 'round', false),
            technician   = context.target && schedule.resolveResourceRecord(context.target),
            calendar     = technician?.calendar;

        if (!technician) {
            return;
        }

        // Validate the drop position
        context.valid = newStartDate && technician.canPerformTask(task, newStartDate);

        // Save reference to the technician so we can use it in onTaskDrop
        context.technician = technician;
    }

    // Drop callback after a mouse up, take action and transfer the unplanned appointment to the real EventStore (if it's valid)
    async onDrop({ context }) {
        const
            me          = this,
            { schedule, grid } = me;

        // If drop was done in a valid location, set the startDate and transfer the task to the Scheduler event store
        if (context.valid) {
            const
                { task, element, technician } = context,
                coordinate            = DomHelper.getTranslateX(element) + (element.offsetWidth / 2),
                dropDate              = schedule.getDateFromCoordinate(coordinate, 'round', false),
                // We schedule the task on the first available time slot for the day
                firstAvailableDaySlot = technician.getFirstAvailableTimeSlot(dropDate, task);

            if (firstAvailableDaySlot) {
                await schedule.scheduleEvent({
                    eventRecord    : task,
                    startDate      : firstAvailableDaySlot,
                    // Assign to the technician (resource) it was dropped on
                    resourceRecord : technician,
                    element
                });

                grid.store.remove(task);
            }
            else {
                context.valid = false;
            }
        }

        schedule.disableScrollingCloseToEdges(schedule.timeAxisSubGrid);
        schedule.features.eventTooltip.disabled = false;
    }
};
