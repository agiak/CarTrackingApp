package com.agcoding.cartrackingapp.domain.usecase.statistics

import com.agcoding.cartrackingapp.domain.model.TopRefill
import com.agcoding.cartrackingapp.domain.model.TopRefillSort
import com.agcoding.cartrackingapp.domain.model.parseStation
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.domain.repository.TripRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * The priciest, thirstiest or most expensive-per-litre refills in a period.
 *
 * The ranking itself is the database's job — it can order and cut the list
 * without handing over the whole history. All that happens here is resolving
 * the ids a refill carries into the names a row has to print.
 */
class GetTopRefillsUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
    private val carRepository: CarRepository,
    private val tripRepository: TripRepository,
) {
    operator fun invoke(
        window: LongRange,
        carIds: Set<Long>,
        sort: TopRefillSort,
        limit: Int = DEFAULT_LIMIT,
    ): Flow<List<TopRefill>> = combine(
        refillRepository.observeTopRefills(window, carIds, sort, limit),
        carRepository.getAllCars(),
        tripRepository.observeTripNames(),
    ) { refills, cars, tripNames ->
        val carNames = cars.associate { it.id to it.name }
        refills.map { refill ->
            val station = parseStation(refill.locationName)
            TopRefill(
                refill = refill,
                carName = carNames[refill.carId],
                stationName = station?.name.orEmpty(),
                stationAddress = station?.address,
                tripName = refill.tripId?.let { tripNames[it] },
            )
        }
    }.flowOn(Dispatchers.Default)

    companion object {
        /**
         * Long enough that scrolling the ranking still feels like a ranking.
         * Past this the list stops being "the expensive ones" and becomes the
         * refill history, which the Transactions screen already is.
         */
        const val DEFAULT_LIMIT = 25
    }
}
