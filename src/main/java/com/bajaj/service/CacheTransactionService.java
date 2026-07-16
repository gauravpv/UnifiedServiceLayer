package com.bajaj.service;

import com.bajaj.entity.BaseTransaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@Service
public class CacheTransactionService {

    @Transactional(readOnly = true)
    public <T extends BaseTransaction> Optional<T> findByHash(
            Function<String, Optional<T>> finder, String hash) {
        return finder.apply(hash);
    }

    @Transactional
    public <T extends BaseTransaction> T save(UnaryOperator<T> saver, T row) {
        return saver.apply(row);
    }
}
