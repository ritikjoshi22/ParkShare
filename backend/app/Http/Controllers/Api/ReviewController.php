<?php

namespace App\Http\Controllers\Api;

use App\Http\Requests\Review\StoreReviewRequest;
use App\Http\Resources\ReviewResource;
use App\Models\Review;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class ReviewController extends ApiController
{
    public function index(Request $request): JsonResponse
    {
        $query = Review::with('user')->latest();

        if ($request->parking_space_id) {
            $query->where('parking_space_id', $request->parking_space_id);
        }

        if ($request->technician_id) {
            $query->where('technician_id', $request->technician_id);
        }

        return $this->success(ReviewResource::collection(
            $query->paginate($request->integer('per_page', 15))
        ));
    }

    public function store(StoreReviewRequest $request): JsonResponse
    {
        $this->authorize('create', Review::class);

        $review = Review::create([
            ...$request->validated(),
            'user_id' => $request->user()->id,
        ]);

        return $this->success(
            new ReviewResource($review->load('user')),
            'Review submitted.',
            201
        );
    }

    public function destroy(Review $review): JsonResponse
    {
        $this->authorize('delete', $review);

        $review->delete();

        return $this->success(null, 'Review deleted.');
    }
}
