import { Box, Card, CardContent, Divider, Typography } from "@mui/material";
import { useEffect, useState } from "react";
import { useParams } from "react-router";
import { getActivity, getActivityRecommendation } from "../services/api";

const ActivityDetail = () => {
  const { id } = useParams();
  const [activity, setActivity] = useState(null);
  const [recommendation, setRecommendation] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchActivityDetail = async () => {
      try {
        setError("");
        const [activityResult, recommendationResult] = await Promise.allSettled([
          getActivity(id),
          getActivityRecommendation(id),
        ]);

        if (activityResult.status === "fulfilled") {
          setActivity(activityResult.value.data);
        } else {
          throw activityResult.reason;
        }

        if (recommendationResult.status === "fulfilled") {
          setRecommendation(recommendationResult.value.data);
        }
      } catch (error) {
        console.error(error);
        setError("Unable to load activity details.");
      }
    };

    fetchActivityDetail();
  }, [id]);

  if (error) {
    return <Typography color="error">{error}</Typography>;
  }

  if (!activity) {
    return <Typography>Loading...</Typography>;
  }

  return (
    <Box sx={{ maxWidth: 800, mx: "auto", p: 2 }}>
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Typography variant="h5" gutterBottom>
            Activity Details
          </Typography>
          <Typography>Type: {activity.type}</Typography>
          <Typography>Duration: {activity.duration} minutes</Typography>
          <Typography>Calories Burned: {activity.caloriesBurned}</Typography>
          <Typography>Date: {new Date(activity.createdAt).toLocaleString()}</Typography>
        </CardContent>
      </Card>

      {recommendation && (
        <Card>
          <CardContent>
            <Typography variant="h5" gutterBottom>
              AI Recommendation
            </Typography>
            <Typography variant="h6">Analysis</Typography>
            <Typography paragraph>{recommendation.recommendation}</Typography>

            <Divider sx={{ my: 2 }} />

            <Typography variant="h6">Improvements</Typography>
            {recommendation.improvements?.map((improvement, index) => (
              <Typography key={index} paragraph>
                - {improvement}
              </Typography>
            ))}

            <Divider sx={{ my: 2 }} />

            <Typography variant="h6">Suggestions</Typography>
            {recommendation.suggestions?.map((suggestion, index) => (
              <Typography key={index} paragraph>
                - {suggestion}
              </Typography>
            ))}

            <Divider sx={{ my: 2 }} />

            <Typography variant="h6">Safety Guidelines</Typography>
            {recommendation.safety?.map((safety, index) => (
              <Typography key={index} paragraph>
                - {safety}
              </Typography>
            ))}
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default ActivityDetail;
