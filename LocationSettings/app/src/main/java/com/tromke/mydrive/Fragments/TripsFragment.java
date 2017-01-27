package com.tromke.mydrive.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tromke.mydrive.Constants.ConstantsSharedPreferences;
import com.tromke.mydrive.Models.Trip;
import com.tromke.mydrive.R;
import com.tromke.mydrive.TripDetailsActivity;
import com.tromke.mydrive.TripsViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TripsFragment extends Fragment {

    FirebaseRecyclerAdapter<Trip, TripsViewHolder> firebaseRecyclerAdapter;
    Query query;

    public TripsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkTrips();
    }

    public void checkTrips() {
        try {
            query = FirebaseDatabase.getInstance().getReference().child("Trip").orderByChild("driverId").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        getActivity().findViewById(R.id.error_layout).setVisibility(View.GONE);
                        getActivity().findViewById(R.id.trips).setVisibility(View.VISIBLE);
                    } else {
                        getActivity().findViewById(R.id.trips).setVisibility(View.GONE);
                        getActivity().findViewById(R.id.error_layout).setVisibility(View.VISIBLE);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (Exception ex) {
            Log.e("Exeption is ", ex.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_trips, container, false);
        RecyclerView trips = (RecyclerView) view.findViewById(R.id.trips);
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Trip, TripsViewHolder>(Trip.class, R.layout.drivers_trip, TripsViewHolder.class, query) {
            @Override
            protected void populateViewHolder(final TripsViewHolder viewHolder, final Trip model, final int position) {
                viewHolder.contactNumber.setText("" + model.booking.phone);
                viewHolder.customerName.setText(model.booking.name);
                viewHolder.timings.setText(model.booking.pick_time);
                viewHolder.tripStatus.setText("Trip " + model.trip_status);
                switch (model.trip_status) {
                    case "pending":
                        break;
                    case "started":
                        viewHolder.rejectTrip.setVisibility(View.GONE);
                        viewHolder.acceptTrip.setText("Trip Details");
                        break;
                    case "accepted":
                        viewHolder.rejectTrip.setVisibility(View.GONE);
                        viewHolder.acceptTrip.setText("Trip Details");
                        break;
                    case "completed":
                        viewHolder.cardBaseLayout.setVisibility(View.GONE);
                        break;
                    case "rejected":
                        viewHolder.cardBaseLayout.setVisibility(View.GONE);
                        break;
                }
                viewHolder.acceptTrip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (model.booking != null) {
                            Intent intent = new Intent(getActivity(), TripDetailsActivity.class);
                            String key = firebaseRecyclerAdapter.getRef(position).getKey();
                            ArrayList<String> bookingDetails = new ArrayList<String>();
                            bookingDetails.add(key);
                            bookingDetails.add(model.booking.name);
                            bookingDetails.add(model.booking.drop_address);
                            bookingDetails.add(model.booking.pickup_address);
                            bookingDetails.add(model.booking.pick_time);
                            bookingDetails.add(String.valueOf(model.booking.phone));
                            intent.putExtra(ConstantsSharedPreferences.INTENT_EXTRA_BOOKING, bookingDetails);
                            switch (model.trip_status) {
                                case "pending":
                                    updateTrip(position, "accepted");
                                    break;
                                default:
                                    break;
                            }
                            startActivity(intent);
                        }
                    }
                });
                viewHolder.rejectTrip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewHolder.cardBaseLayout.setVisibility(View.GONE);
                        updateTrip(position, "rejected");

                    }
                });
            }
        };

        trips.setHasFixedSize(true);
        trips.setLayoutManager(new LinearLayoutManager(getActivity()));
        trips.setAdapter(firebaseRecyclerAdapter);

        view.findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseRecyclerAdapter != null) {
                    firebaseRecyclerAdapter.notifyDataSetChanged();
                }
                checkTrips();
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        firebaseRecyclerAdapter.cleanup();
    }

    void updateTrip(int pos, final String value) {
        String key = firebaseRecyclerAdapter.getRef(pos).getKey();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Trip");
        Query query = reference.orderByKey().equalTo(key);
        //  Query query = FirebaseDatabase.getInstance().getReference().child("trips").orderByChild("tripId").equalTo(tripId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        Map<String, Object> tripStatus = new HashMap();

                        tripStatus.put("trip_status", value);
                        FirebaseDatabase.getInstance().getReference().child("Trip").child(childDataSnapshot.getKey()).updateChildren(tripStatus);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        if (firebaseRecyclerAdapter != null) {
            getActivity().findViewById(R.id.trips).setVisibility(View.VISIBLE);
            firebaseRecyclerAdapter.notifyDataSetChanged();
        }
        checkTrips();
    }
}
