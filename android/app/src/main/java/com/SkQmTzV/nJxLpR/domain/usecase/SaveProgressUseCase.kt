package com.SkQmTzV.nJxLpR.domain.usecase

import com.SkQmTzV.nJxLpR.domain.model.RoundResult
import com.SkQmTzV.nJxLpR.domain.repository.ProgressRepository

class SaveProgressUseCase(private val progressRepository: ProgressRepository) {

    operator fun invoke(result: RoundResult, levelCount: Int) {
        progressRepository.store(result.levelId, result.stars, result.score)
        if (result.win && result.levelId < levelCount) {
            progressRepository.unlock(result.levelId + 1)
        }
    }
}
