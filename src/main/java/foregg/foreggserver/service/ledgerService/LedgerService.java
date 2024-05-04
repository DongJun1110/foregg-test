package foregg.foreggserver.service.ledgerService;

import foregg.foreggserver.apiPayload.exception.handler.LedgerHandler;
import foregg.foreggserver.converter.LedgerConverter;
import foregg.foreggserver.domain.Ledger;
import foregg.foreggserver.domain.User;
import foregg.foreggserver.dto.ledgerDTO.LedgerRequestDTO;
import foregg.foreggserver.jwt.SecurityUtil;
import foregg.foreggserver.repository.LedgerRepository;
import foregg.foreggserver.service.userService.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static foregg.foreggserver.apiPayload.code.status.ErrorStatus.LEDGER_NOT_FOUND;

@Transactional
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final UserQueryService userQueryService;

    public void add(LedgerRequestDTO dto) {
        User user = userQueryService.getUser(SecurityUtil.getCurrentUser());
        ledgerRepository.save(LedgerConverter.toLedger(dto, user));
    }

    public void modify(Long id, LedgerRequestDTO dto) {
        Ledger ledger = ledgerRepository.findById(id).orElseThrow(() -> new LedgerHandler(LEDGER_NOT_FOUND));
        ledger.updateLedger(dto);
    }

    public void delete(Long id) {
        Ledger ledger = ledgerRepository.findById(id).orElseThrow(() -> new LedgerHandler(LEDGER_NOT_FOUND));
        ledgerRepository.delete(ledger);
    }

}
