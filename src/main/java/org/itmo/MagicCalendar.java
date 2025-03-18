package org.itmo;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class MagicCalendar {


    private final Map<String, List<Meeting>> calendar = new HashMap<>();

    private static class Meeting {
        private final LocalTime start;
        private final MeetingType meetingType;

        public Meeting(final LocalTime start, final MeetingType meetingType) {
            this.start = start;
            this.meetingType = meetingType;
        }

        public boolean intersects(final LocalTime other) {

            if (start.equals(other) || start.isAfter(other) && start.plusHours(1).isBefore(other)) {
                return true;
            }
            return false;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Meeting meeting = (Meeting) o;
            return Objects.equals(start, meeting.start);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(start);
        }
    }


    // Перечисление типов встреч
    public enum MeetingType {
        WORK, PERSONAL
    }

    /**
     * Запланировать встречу для пользователя.
     *
     * @param user имя пользователя
     * @param time временной слот (например, "10:00")
     * @param type тип встречи (WORK или PERSONAL)
     * @return true, если встреча успешно запланирована, false если:
     * - в этот временной слот уже есть встреча, и правило замены не выполняется,
     * - лимит в 5 встреч в день уже достигнут.
     */
    public boolean scheduleMeeting(String user, String time, MeetingType type) {
        if (!calendar.containsKey(user)) {
            calendar.put(user, new ArrayList<>());
        }
        try {
            final LocalTime parsedTime = LocalTime.parse(time);
            final List<Meeting> meetings = calendar.get(user);

            if (meetings.size() >= 5) {
                return false;
            }

            final Meeting intersect = getIntersect(meetings, parsedTime);

            if (intersect != null) {
                if (MeetingType.WORK.equals(type)) {
                    if (intersect.meetingType == MeetingType.PERSONAL) {
                        return false;
                    } else {
                        calendar.get(user).remove(intersect);
                        addMeeting(user, type, parsedTime);
                    }
                } else {
                    calendar.get(user).remove(intersect);
                    addMeeting(user, type, parsedTime);
                }
            } else {
                calendar.get(user);
            }


        } catch (Exception e) {
            return false;
        }

        // Реализация метода
        return true;
    }

    private Meeting getIntersect(List<Meeting> meetings, LocalTime start) {
        return meetings.stream().filter(meeting -> meeting.intersects(start)).findFirst().orElse(null);
    }

    private void addMeeting(String user, MeetingType type, LocalTime start) {
        if (!calendar.containsKey(user)) {
            calendar.put(user, new ArrayList<>());
        }
        calendar.get(user).add(new Meeting(start, type));
    }

    /**
     * Получить список всех встреч пользователя.
     *
     * @param user имя пользователя
     * @return список временных слотов, на которые запланированы встречи.
     */
    public List<String> getMeetings(String user) {
        // Реализация метода
        if (!calendar.containsKey(user)) {
            return new ArrayList<>();
        }
        return calendar.get(user).stream().map(meeting -> meeting.start.toString()).collect(Collectors.toList());
    }

    /**
     * Отменить встречу для пользователя по заданному времени.
     *
     * @param user имя пользователя
     * @param time временной слот, который нужно отменить.
     * @return true, если встреча была успешно отменена; false, если:
     * - встреча в указанное время отсутствует,
     * - встреча имеет тип PERSONAL (отменять можно только WORK встречу).
     */
    public boolean cancelMeeting(String user, String time) {
        // Реализация метода
        if (!calendar.containsKey(user)) {
            return false;
        }
        final LocalTime parsedTime = LocalTime.parse(time);

        final List<Meeting> meetings = calendar.get(user);
        final Optional<Meeting> first = meetings.stream().filter(meeting -> meeting.start.isAfter(parsedTime)).findFirst();
        if (first.isPresent()) {
            if (first.get().meetingType == MeetingType.PERSONAL) {
               return false;
            }
            calendar.get(user).remove(first.get());
            return true;
        }

        return false;
    }
}
