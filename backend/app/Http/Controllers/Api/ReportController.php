<?php

namespace App\Http\Controllers\Api;

use App\Http\Requests\Report\StoreReportRequest;
use App\Http\Resources\ReportResource;
use App\Models\Report;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class ReportController extends ApiController
{
    public function index(Request $request): JsonResponse
    {
        $this->authorize('viewAny', Report::class);

        $query = Report::with(['reporter', 'booking', 'parkingSpace'])->latest();

        if (! $request->user()->isAdmin()) {
            $query->where('reported_by', $request->user()->id);
        }

        if ($request->status) {
            $query->where('status', $request->status);
        }

        return $this->success(ReportResource::collection(
            $query->paginate($request->integer('per_page', 15))
        ));
    }

    public function store(StoreReportRequest $request): JsonResponse
    {
        $this->authorize('create', Report::class);

        $report = Report::create([
            ...$request->validated(),
            'reported_by' => $request->user()->id,
            'status' => 'pending',
        ]);

        return $this->success(
            new ReportResource($report->load(['reporter', 'booking', 'parkingSpace'])),
            'Report submitted.',
            201
        );
    }

    public function show(Report $report): JsonResponse
    {
        $this->authorize('view', $report);

        return $this->success(new ReportResource($report->load(['reporter', 'booking', 'parkingSpace'])));
    }

    public function resolve(Report $report): JsonResponse
    {
        $this->authorize('resolve', $report);

        $report->update(['status' => 'resolved']);

        return $this->success(new ReportResource($report->fresh()), 'Report resolved.');
    }

    public function destroy(Report $report): JsonResponse
    {
        $this->authorize('delete', $report);

        $report->delete();

        return $this->success(null, 'Report deleted.');
    }
}
