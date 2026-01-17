const handleProjectSearch = async () => {
    setError("");
    setLoading(true);
    setEmployeeData(null);
    setProjectResults(null);

    try {
        const res = await fetch(
            `${baseUrl}/api/projects`,
            {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            }
        );

        if (!res.ok) throw new Error("Error al cargar proyectos");

        const data = await res.json();
        setProjectResults(data.content ?? []);
    } catch (err) {
        console.error(err);
        setError("No se pudieron cargar los proyectos");
        setProjectResults([]);
    } finally {
        setLoading(false);
    }
};
