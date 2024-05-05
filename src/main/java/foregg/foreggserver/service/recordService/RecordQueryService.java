package foregg.foreggserver.service.recordService;

import foregg.foreggserver.apiPayload.exception.handler.UserHandler;
import foregg.foreggserver.converter.HomeConverter;
import foregg.foreggserver.converter.RecordConverter;
import foregg.foreggserver.domain.Record;
import foregg.foreggserver.domain.RepeatTime;
import foregg.foreggserver.domain.User;
import foregg.foreggserver.dto.homeDTO.HomeRecordResponseDTO;
import foregg.foreggserver.dto.homeDTO.HomeResponseDTO;
import foregg.foreggserver.dto.recordDTO.RecordResponseDTO;
import foregg.foreggserver.dto.recordDTO.ScheduleResponseDTO;
import foregg.foreggserver.jwt.SecurityUtil;
import foregg.foreggserver.repository.RecordRepository;
import foregg.foreggserver.repository.RepeatTimeRepository;
import foregg.foreggserver.repository.UserRepository;
import foregg.foreggserver.service.userService.UserQueryService;
import foregg.foreggserver.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static foregg.foreggserver.apiPayload.code.status.ErrorStatus.USER_NOT_FOUND;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class RecordQueryService {

    private final UserRepository userRepository;
    private final RecordRepository recordRepository;
    private final RepeatTimeRepository repeatTimeRepository;
    private final UserQueryService userQueryService;

    public ScheduleResponseDTO calendar(String yearmonth) {
        List<String> adjacentMonths = DateUtil.getAdjacentMonths(yearmonth);
        User user = getUser(SecurityUtil.getCurrentUser());
        List<RecordResponseDTO> recordList = new ArrayList<>();

        for (String s : adjacentMonths) {
            Optional<List<Record>> result = recordRepository.findByUserAndYearmonth(user, s);
            if (result.isPresent()) {
                List<Record> records = result.get();
                for (Record record : records) {
                    RecordResponseDTO resultDTO = getRepeatTimes(record);
                    // 중복 체크
                    if (!recordList.stream().anyMatch(dto -> dto.getId() == resultDTO.getId())) {
                        recordList.add(resultDTO);
                    }
                }
            }
        }

        for (String s : adjacentMonths) {
            Optional<List<Record>> result = recordRepository.findByUser(user);
            if (result.isPresent()) {
                List<Record> records = result.get();
                for (Record record : records) {
                    List<String> startEndYearmonth = record.getStart_end_yearmonth();
                    RecordResponseDTO resultDTO = getRepeatTimes(record);
                    if (startEndYearmonth != null && startEndYearmonth.contains(s)) {
                        // 중복 체크
                        if (!recordList.stream().anyMatch(dto -> dto.getId() == resultDTO.getId())) {
                            recordList.add(resultDTO);
                        }
                    }
                }
            }
        }

        return ScheduleResponseDTO.builder().records(recordList).build();
    }


    private User getUser(String keycode) {
        User user = userRepository.findByKeyCode(keycode).orElseThrow(() -> new UserHandler(USER_NOT_FOUND));
        return user;
    }

    private RecordResponseDTO getRepeatTimes(Record record) {
        Optional<List<RepeatTime>> repeatTimes = repeatTimeRepository.findByRecord(record);
        if(repeatTimes.isPresent()){
            List<RepeatTime> result = repeatTimes.get();
            return RecordConverter.toRecordResponseDTO(record, result);
        }
        return RecordConverter.toRecordResponseDTO(record, null);
    }

    public HomeResponseDTO getTodayRecord() {
        List<HomeRecordResponseDTO> resultList = new ArrayList<>();
        User user = userQueryService.getUser(SecurityUtil.getCurrentUser());
        String todayDate = DateUtil.formatLocalDateTime(LocalDate.now());
        Optional<List<Record>> foundRecord = recordRepository.findByUser(user);

        if(foundRecord.isEmpty()){
            return null;
        }

        List<Record> records = foundRecord.get();
        for (Record record : records) {
            if (record.getDate() == null) {
                //반복주기가 설정된 일정
                List<String> intervalDates = DateUtil.getIntervalDates(record.getStart_date(), record.getEnd_date());
                if (intervalDates.contains(todayDate)) {
                    resultList.add(HomeConverter.toHomeRecordResponseDTO(record));
                }
            }else{
                //반복주기가 설정되지 않은 일정
                if (record.getDate().equals(todayDate)) {
                    resultList.add(HomeConverter.toHomeRecordResponseDTO(record));
                }
            }
        }
        return HomeConverter.toHomeResponseDTO(user.getNickname(), todayDate, resultList);
    }


}